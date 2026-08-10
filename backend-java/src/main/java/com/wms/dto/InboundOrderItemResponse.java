package com.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundOrderItemResponse {
    /** 商品 ID */
    private Long productId;
    /** 商品名称 */
    private String productName;
    /** 入库数量 */
    private Integer quantity;
    /** 目标库位编码 */
    private String locationCode;
}
