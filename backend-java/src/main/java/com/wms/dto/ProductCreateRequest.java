package com.wms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新增商品请求体 */
@Data
public class ProductCreateRequest {
    /** 商品名称，必填，最长 200 字符 */
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称最长200个字符")
    private String name;

    /** 商品 SKU，必填，全局唯一（后端会校验） */
    @NotBlank(message = "SKU不能为空")
    @Size(max = 50, message = "SKU最长50个字符")
    private String sku;

    /** 计量单位，默认“个” */
    private String unit = "个";
}
