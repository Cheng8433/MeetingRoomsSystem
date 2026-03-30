// 文件位置：src/main/java/com/example/dto/ApiResponse.java
package com.myGroup.dto;

/**
 * 统一API响应格式
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String code; // 可选：可以添加状态码

    // 默认构造方法
    public ApiResponse() {
    }

    // 全参数构造方法
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message, T data, String code) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    // 成功响应的静态方法
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data, "200");
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, "200");
    }

    // 失败响应的静态方法
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, "500");
    }

    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(false, message, null, code);
    }

    // getter 和 setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}