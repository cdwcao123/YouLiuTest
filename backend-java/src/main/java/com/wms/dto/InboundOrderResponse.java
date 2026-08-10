package com.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundOrderResponse {
    /** 单据 ID */
    private Long id;
    /** 入库单号 IN-YYYYMMDD-XXX */
    private String orderNo;
    /** 供应商名称 */
    private String supplierName;
    /** 状态：COMPLETED */
    private String status;
    /** 明细行 */
    private List<InboundOrderItemResponse> items;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
