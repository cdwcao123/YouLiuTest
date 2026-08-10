package com.wms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 更新商品请求体：SKU 不可修改，因此这里没有 sku 字段 */
@Data
public class ProductUpdateRequest {
    /** 商品名称，必填 */
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200)
    private String name;

    /** 计量单位（可空，空则保持原值） */
    @Size(max = 20)
    private String unit;
}
