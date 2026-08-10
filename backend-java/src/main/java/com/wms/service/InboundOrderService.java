package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.dto.InboundOrderCreateRequest;
import com.wms.dto.InboundOrderItemResponse;
import com.wms.dto.InboundOrderResponse;
import com.wms.entity.InboundOrder;
import com.wms.entity.InboundOrderItem;
import com.wms.entity.Product;
import com.wms.repository.InboundOrderItemRepository;
import com.wms.repository.InboundOrderRepository;
import com.wms.repository.InventoryRepository;
import com.wms.repository.LocationRepository;
import com.wms.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 入库单 Service — 任务 1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundOrderService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InboundOrderRepository inboundOrderRepository;
    private final InboundOrderItemRepository inboundOrderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;

    /**
     * 创建入库单并累加库存，整个操作在一个数据库事务内完成。
     */
    @Transactional
    public InboundOrderResponse createInboundOrder(InboundOrderCreateRequest request) {
        Map<Long, Product> products = validateAndLoad(request);

        InboundOrder order = InboundOrder.builder()
                .orderNo(generateOrderNo())
                .supplierName(request.getSupplierName().trim())
                .status("COMPLETED")
                .build();
        order = inboundOrderRepository.save(order);

        List<InboundOrderItemResponse> itemResponses = new ArrayList<>();
        for (InboundOrderCreateRequest.InboundItemRequest item : request.getItems()) {
            inboundOrderItemRepository.save(InboundOrderItem.builder()
                    .orderId(order.getId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .locationCode(item.getLocationCode().trim())
                    .build());

            // 原子累加库存：INSERT ... ON DUPLICATE KEY UPDATE，并发入库不会丢失更新
            inventoryRepository.increaseQuantity(
                    item.getProductId(), item.getLocationCode().trim(), item.getQuantity());

            itemResponses.add(InboundOrderItemResponse.builder()
                    .productId(item.getProductId())
                    .productName(products.get(item.getProductId()).getName())
                    .quantity(item.getQuantity())
                    .locationCode(item.getLocationCode().trim())
                    .build());
        }

        log.info("创建入库单成功: orderNo={}, 明细数={}", order.getOrderNo(), request.getItems().size());
        return toResponse(order, itemResponses);
    }

    /**
     * 入库单列表（分页）
     */
    public PageResult<InboundOrderResponse> list(int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Page<InboundOrder> orders = inboundOrderRepository.findAll(PageRequest.of(safePage - 1, safeSize));

        List<Long> orderIds = orders.getContent().stream().map(InboundOrder::getId).toList();
        Map<Long, List<InboundOrderItem>> itemsByOrder = orderIds.isEmpty()
                ? Map.of()
                : inboundOrderItemRepository.findByOrderIdIn(orderIds).stream()
                        .collect(Collectors.groupingBy(InboundOrderItem::getOrderId));
        Map<Long, Product> products = loadProducts(itemsByOrder.values().stream()
                .flatMap(List::stream)
                .map(InboundOrderItem::getProductId)
                .collect(Collectors.toSet()));

        List<InboundOrderResponse> list = orders.getContent().stream()
                .map(o -> toResponse(o, itemsByOrder.getOrDefault(o.getId(), List.of()), products))
                .toList();
        return new PageResult<>(list, orders.getTotalElements(), safePage, safeSize);
    }

    /**
     * 入库单详情
     */
    public InboundOrderResponse getById(Long id) {
        InboundOrder order = inboundOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "入库单不存在"));
        List<InboundOrderItem> items = inboundOrderItemRepository.findByOrderId(id);
        Map<Long, Product> products = loadProducts(items.stream()
                .map(InboundOrderItem::getProductId)
                .collect(Collectors.toSet()));
        return toResponse(order, items, products);
    }

    /**
     * 校验明细非空、商品存在、库位存在，
     * 并一次性加载商品实体，返回 productId -> Product 映射供响应组装使用。
     */
    private Map<Long, Product> validateAndLoad(InboundOrderCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("入库明细不能为空");
        }
        Map<Long, Product> products = request.getItems().stream()
                .map(item -> productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new BusinessException(404, "商品不存在: id=" + item.getProductId())))
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        for (InboundOrderCreateRequest.InboundItemRequest item : request.getItems()) {
            if (!locationRepository.existsByCode(item.getLocationCode().trim())) {
                throw new BusinessException(404, "库位不存在: " + item.getLocationCode());
            }
        }
        return products;
    }

    /** 批量加载商品，避免逐条查询的 N+1 问题 */
    private Map<Long, Product> loadProducts(Set<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    /**
     * 生成入库单号：IN-YYYYMMDD-XXX（XXX 为当天序号，从 001 开始，三位补零）
     */
    String generateOrderNo() {
        String prefix = "IN-" + LocalDate.now().format(DAY) + "-";
        return prefix + nextSequence(prefix);
    }

    /** 取当天最大单号的后三位序号 +1；当天无单则从 001 开始 */
    private String nextSequence(String prefix) {
        int seq = inboundOrderRepository
                .findTopByOrderNoStartingWithOrderByOrderNoDesc(prefix)
                .map(InboundOrder::getOrderNo)
                .map(orderNo -> {
                    try {
                        return Integer.parseInt(orderNo.substring(orderNo.length() - 3)) + 1;
                    } catch (NumberFormatException e) {
                        return 1;
                    }
                })
                .orElse(1);
        return String.format("%03d", seq);
    }

    /** 实体 -> 响应 DTO（详情/列表场景，明细来自查询结果） */
    private InboundOrderResponse toResponse(InboundOrder order,
                                            List<InboundOrderItem> items,
                                            Map<Long, Product> products) {
        List<InboundOrderItemResponse> itemResponses = items.stream()
                .map(item -> InboundOrderItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(products.getOrDefault(item.getProductId(), new Product()).getName())
                        .quantity(item.getQuantity())
                        .locationCode(item.getLocationCode())
                        .build())
                .toList();
        return InboundOrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .supplierName(order.getSupplierName())
                .status(order.getStatus())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }

    /** 实体 -> 响应 DTO（创建场景，明细是刚组装好的响应对象） */
    private InboundOrderResponse toResponse(InboundOrder order,
                                            List<InboundOrderItemResponse> itemResponses) {
        return InboundOrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .supplierName(order.getSupplierName())
                .status(order.getStatus())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
