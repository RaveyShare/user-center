package com.ravey.ai.user.api.model.res;

import lombok.Data;

/**
 * 小程序登录响应DTO
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
public class MiniProgramLoginRes {

    /**
     * JWT令牌
     */
    private String token;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    /**
     * 用户信息内部类
     */
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
    }
}