package com.ravey.ai.user.api.model.req;

import lombok.Data;

/**
 * 邮箱登录请求
 *
 * @author Ravey
 */
@Data
public class EmailLoginReq {
    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 密码（密码登录时必填）
     */
    private String password;

    /**
     * 验证码（验证码登录时必填）
     */
    private String code;

    /**
     * 登录方式：1-密码登录，2-验证码登录
     */
    private Integer loginType;
}
