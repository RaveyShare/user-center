package com.ravey.ai.user.api.dto;

import lombok.Data;

/**
 * 应用数据传输对象（DTO）
 * 用于API层的数据传输，只包含API层需要的字段
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
public class AppsDTO {
    
    /**
     * 应用ID
     */
    private Long id;
    
    /**
     * 应用标识
     */
    private String appId;
    
    /**
     * 应用名称
     */
    private String appName;
    
    /**
     * 应用密钥
     */
    private String appSecret;
    
    /**
     * 应用描述
     */
    private String description;
    
    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;
}