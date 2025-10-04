package com.ravey.ai.user.api.model.req;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 小程序登录请求DTO
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
public class MiniProgramLoginReq {

    /**
     * 小程序AppId
     */
    @NotBlank(message = "小程序AppId不能为空")
    private String appId;

    /**
     * 微信授权码
     */
    @NotBlank(message = "微信授权码不能为空")
    private String code;

    /**
     * 用户信息（可选）
     */
    private UserInfo userInfo;

    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfo {
        /**
         * 昵称
         */
        private String nickname;

        /**
         * 头像URL
         */
        private String avatarUrl;
    }
}