package com.ravey.ai.user.api.model.res;

import lombok.Data;

/**
 * 登录响应DTO
 *
 * @author Ravey
 */
@Data
public class LoginRes {

    /**
     * JWT令牌
     */
    private String token;

    /**
     * 令牌过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private Long id;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 头像URL
         */
        private String avatarUrl;

        /**
         * 邮箱
         */
        private String email;
    }
}
