package com.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 商品响应 DTO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    /** 商品 ID */
    private Long id;
    /** 商品名称 */
    private String name;
    /** 商品 SKU */
    private String sku;
    /** 计量单位 */
    private String unit;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
