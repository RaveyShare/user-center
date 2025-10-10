package com.ravey.ai.user.api.vo;

import lombok.Data;

/**
 * 统一响应结果
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
public class Result<T> {
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功标识
     */
    private Boolean success;
    
    public Result() {}
    
    public Result(Integer code, String message, T data, Boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }
    
    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, true);
    }
    
    /**
     * 成功响应
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data, true);
    }
    
    /**
     * 错误响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null, false);
    }
    
    /**
     * 错误响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null, false);
    }
}