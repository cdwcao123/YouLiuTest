package com.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundOrderResponse {
    /** 单据 ID */
    private Long id;
    /** 出库单号 OUT-YYYYMMDD-XXX */
    private String orderNo;
    /** 客户名称 */
    private String customerName;
    /** 状态：COMPLETED */
    private String status;
    /** 明细行 */
    private List<OutboundOrderItemResponse> items;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
