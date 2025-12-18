package com.ravey.ai.user.api.model.req;

import lombok.Data;

/**
 * 邮件验证码请求
 *
 * @author Ravey
 */
@Data
public class EmailSendCodeReq {
    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 场景：1-注册，2-登录，3-重置密码
     */
    private Integer scene;
}
