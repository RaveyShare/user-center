package com.ravey.ai.user.api.model.res;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用响应DTO
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
public class AppRes {

    /**
     * 应用ID
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 微信小程序AppId
     */
    private String appId;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}