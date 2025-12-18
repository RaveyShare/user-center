package com.ravey.ai.user.api.model.req;

import lombok.Data;

/**
 * 邮箱注册请求
 *
 * @author Ravey
 */
@Data
public class EmailRegisterReq {
    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * 昵称
     */
    private String nickname;
}
