package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.dto.OutboundOrderCreateRequest;
import com.wms.dto.OutboundOrderItemResponse;
import com.wms.dto.OutboundOrderResponse;
import com.wms.entity.Inventory;
import com.wms.entity.OutboundOrder;
import com.wms.entity.OutboundOrderItem;
import com.wms.entity.Product;
import com.wms.repository.InventoryRepository;
import com.wms.repository.LocationRepository;
import com.wms.repository.OutboundOrderItemRepository;
import com.wms.repository.OutboundOrderRepository;
import com.wms.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 出库单 Service — 选做任务 A
 *
 * 并发安全方案：对库存行加悲观写锁（SELECT ... FOR UPDATE），
 * 锁住后再检查库存并扣减，保证同一库位同一商品的并发出库串行执行，不会超卖。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundOrderService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OutboundOrderRepository outboundOrderRepository;
    private final OutboundOrderItemRepository outboundOrderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;

    /**
     * 创建出库单：
     * 1. 校验商品/库位；
     * 2. 逐行加锁扣减库存（悲观锁）；
     * 3. 保存出库单与明细。
     * 整个方法在一个事务内，任一步失败都会整体回滚。
     */
    @Transactional
    public OutboundOrderResponse createOutboundOrder(OutboundOrderCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("出库明细不能为空");
        }
        Map<Long, Product> products = request.getItems().stream()
                .map(item -> productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new BusinessException(404, "商品不存在: id=" + item.getProductId())))
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        for (OutboundOrderCreateRequest.OutboundItemRequest item : request.getItems()) {
            if (!locationRepository.existsByCode(item.getLocationCode().trim())) {
                throw new BusinessException(404, "库位不存在: " + item.getLocationCode());
            }
        }

        List<OutboundOrderItemResponse> itemResponses = new ArrayList<>();
        for (OutboundOrderCreateRequest.OutboundItemRequest item : request.getItems()) {
            deductInventory(item.getProductId(), item.getLocationCode().trim(), item.getQuantity());
        }

        OutboundOrder order = OutboundOrder.builder()
                .orderNo(generateOrderNo())
                .customerName(request.getCustomerName().trim())
                .status("COMPLETED")
                .build();
        order = outboundOrderRepository.save(order);

        for (OutboundOrderCreateRequest.OutboundItemRequest item : request.getItems()) {
            outboundOrderItemRepository.save(OutboundOrderItem.builder()
                    .orderId(order.getId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .locationCode(item.getLocationCode().trim())
                    .build());

            itemResponses.add(OutboundOrderItemResponse.builder()
                    .productId(item.getProductId())
                    .productName(products.get(item.getProductId()).getName())
                    .quantity(item.getQuantity())
                    .locationCode(item.getLocationCode().trim())
                    .build());
        }

        log.info("创建出库单成功: orderNo={}, 明细数={}", order.getOrderNo(), request.getItems().size());
        return toResponse(order, itemResponses);
    }

    /**
     * 扣减库存：悲观锁 + 库存充足校验。
     */
    private void deductInventory(Long productId, String locationCode, Integer quantity) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndLocationCodeForUpdate(productId, locationCode)
                .orElseThrow(() -> new BusinessException(
                        "库存不足: 商品ID=" + productId + ", 库位=" + locationCode + " 无库存记录"));
        if (inventory.getQuantity() < quantity) {
            throw new BusinessException("库存不足: 商品ID=" + productId
                    + ", 库位=" + locationCode
                    + " 当前库存=" + inventory.getQuantity()
                    + ", 需出库=" + quantity);
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    /**
     * 生成出库单号：OUT-YYYYMMDD-XXX
     */
    String generateOrderNo() {
        String prefix = "OUT-" + LocalDate.now().format(DAY) + "-";
        int seq = outboundOrderRepository
                .findTopByOrderNoStartingWithOrderByOrderNoDesc(prefix)
                .map(OutboundOrder::getOrderNo)
                .map(orderNo -> {
                    try {
                        return Integer.parseInt(orderNo.substring(orderNo.length() - 3)) + 1;
                    } catch (NumberFormatException e) {
                        return 1;
                    }
                })
                .orElse(1);
        return prefix + String.format("%03d", seq);
    }

    /** 实体 -> 响应 DTO */
    private OutboundOrderResponse toResponse(OutboundOrder order,
                                             List<OutboundOrderItemResponse> itemResponses) {
        return OutboundOrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .customerName(order.getCustomerName())
                .status(order.getStatus())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
