package com.ravey.ai.user.api.model.req;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 用户信息更新请求
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
public class UserUpdateReq {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;
}
