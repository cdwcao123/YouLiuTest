package com.wms.common;

/**
 * 业务异常：业务规则被违反时抛出（如库存不足、商品/库位不存在、重复 SKU）。
 * 由 {@link GlobalExceptionHandler} 统一捕获并转换为 { code, message } 响应。
 * code 默认 400，需要更精确状态时可传入 404 等。
 */
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** 默认按 400 业务错误处理 */
    public BusinessException(String message) {
        this(400, message);
    }

    public int getCode() {
        return code;
    }
}
