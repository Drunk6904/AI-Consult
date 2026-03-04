package com.zhuofeng.ai_consult_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用接口返回结构。
 * code=0 表示成功，非0表示失败。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 业务状态码。
     */
    private int code;

    /**
     * 返回消息。
     */
    private String message;

    /**
     * 返回数据。
     */
    private T data;

    /**
     * 快速返回成功结构。
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    /**
     * 快速返回失败结构（默认code=1）。
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(1, message, null);
    }

    /**
     * 快速返回失败结构（可指定code）。
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
