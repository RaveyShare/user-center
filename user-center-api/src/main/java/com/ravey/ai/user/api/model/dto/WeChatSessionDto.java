package com.ravey.ai.user.api.model.dto;

import lombok.Data;

/**
 * 微信会话信息DTO
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
public class WeChatSessionDto {

    /**
     * 用户唯一标识
     */
    private String openid;

    /**
     * 会话密钥
     */
    private String sessionKey;

    /**
     * 用户在开放平台的唯一标识符
     */
    private String unionid;

    /**
     * 错误码
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;
}