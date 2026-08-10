package com.wms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 入库单创建请求 — 任务 1
 */
@Data
public class InboundOrderCreateRequest {

    /** 供应商名称，必填 */
    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    /** 入库明细列表，至少一行；@Valid 会继续校验每行的字段 */
    @NotEmpty(message = "入库明细不能为空")
    @Valid
    private List<InboundItemRequest> items;

    /** 入库单的一行明细：商品 + 数量 + 目标库位 */
    @Data
    public static class InboundItemRequest {
        /** 商品 ID，对应 products.id */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /** 入库数量，必须大于 0 */
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于0")
        private Integer quantity;

        /** 目标库位编码，对应 locations.code */
        @NotBlank(message = "库位编码不能为空")
        private String locationCode;
    }
}
