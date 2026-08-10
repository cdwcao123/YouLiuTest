package com.wms.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应包装：所有接口都返回 { code, message, data } 结构。
 * code 语义：200 成功 / 201 创建成功 / 400 业务错误 / 404 资源不存在 / 409 数据冲突 / 500 服务器错误。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** 业务状态码，与 HTTP 状态码语义保持一致 */
    private int code;

    /** 给调用方的提示信息 */
    private String message;

    /** 业务数据，失败时为 null */
    private T data;

    /** 成功（200），默认提示 "success" */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /** 成功（200），自定义提示信息 */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /** 创建成功（201），用于 POST 新建接口 */
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data);
    }

    /** 失败响应 */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
