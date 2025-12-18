package com.ravey.ai.user.api.model.req;

import lombok.Data;

/**
 * 密码重置请求
 *
 * @author Ravey
 */
@Data
public class PasswordResetReq {
    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 验证码
     */
    private String code;
}
