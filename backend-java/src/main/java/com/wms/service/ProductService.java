package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.dto.ProductCreateRequest;
import com.wms.dto.ProductResponse;
import com.wms.dto.ProductUpdateRequest;
import com.wms.entity.Product;
import com.wms.repository.InboundOrderItemRepository;
import com.wms.repository.InventoryRepository;
import com.wms.repository.OutboundOrderItemRepository;
import com.wms.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品管理 Service — 参考实现
 * 展示了：参数校验、异常处理、事务管理、DTO 转换
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InboundOrderItemRepository inboundOrderItemRepository;
    private final OutboundOrderItemRepository outboundOrderItemRepository;

    /** 商品全量列表（不分页，供下拉搜索等轻量场景使用） */
    public List<ProductResponse> list(String keyword) {
        List<Product> products = productRepository.search(keyword);
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 商品分页查询（与 API 规范一致：keyword/page/pageSize）
     */
    public PageResult<ProductResponse> page(String keyword, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Product> result = productRepository.searchPage(kw, PageRequest.of(safePage - 1, safeSize));
        List<ProductResponse> list = result.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(list, result.getTotalElements(), safePage, safeSize);
    }

    /** 商品详情，不存在时抛 404 业务异常 */
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        return toResponse(product);
    }

    /** 新增商品：SKU 唯一性校验 + 事务保存 */
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("SKU已存在: " + request.getSku());
        }
        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .unit(request.getUnit() != null ? request.getUnit() : "个")
                .build();
        product = productRepository.save(product);
        log.info("创建商品成功: id={}, sku={}", product.getId(), product.getSku());
        return toResponse(product);
    }

    /** 更新商品：仅允许修改名称/单位 */
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        product.setName(request.getName());
        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }
        product = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(404, "商品不存在");
        }
        // Bug 修复：删除前校验商品是否存在关联库存，避免库存数据孤立
        if (inventoryRepository.existsByProductId(id)) {
            throw new BusinessException("该商品存在关联库存，无法删除");
        }
        // 同时校验出入库单明细引用，保证历史单据数据完整
        if (inboundOrderItemRepository.existsByProductId(id)
                || outboundOrderItemRepository.existsByProductId(id)) {
            throw new BusinessException("该商品存在关联出入库单，无法删除");
        }
        productRepository.deleteById(id);
        log.info("删除商品: id={}", id);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .unit(product.getUnit())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
