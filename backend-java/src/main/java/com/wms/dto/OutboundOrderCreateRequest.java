package com.wms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 出库单创建请求 — 选做任务 A
 */
@Data
public class OutboundOrderCreateRequest {

    /** 客户名称，必填 */
    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    /** 出库明细列表，至少一行 */
    @NotEmpty(message = "出库明细不能为空")
    @Valid
    private List<OutboundItemRequest> items;

    /** 出库单的一行明细：商品 + 数量 + 来源库位 */
    @Data
    public static class OutboundItemRequest {
        /** 商品 ID */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /** 出库数量，必须大于 0 */
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于0")
        private Integer quantity;

        /** 来源库位编码 */
        @NotBlank(message = "库位编码不能为空")
        private String locationCode;
    }
}
