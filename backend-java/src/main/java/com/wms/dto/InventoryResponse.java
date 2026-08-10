package com.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 库存查询响应行：商品 + 库位 + 仓库 + 数量 + 更新时间。
 * 由 InventoryRepository.searchInventory 的 JPQL 构造函数表达式直接映射。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    /** 商品 ID */
    private Long productId;
    /** 商品名称 */
    private String productName;
    /** 商品 SKU */
    private String sku;
    /** 供应商名称 */
    private String supplierName;
    /** 库位编码 */
    private String locationCode;
    /** 所属仓库名称 */
    private String warehouseName;
    /** 当前库存数量 */
    private Integer quantity;
    /** 库存最后更新时间 */
    private LocalDateTime updatedAt;
}
