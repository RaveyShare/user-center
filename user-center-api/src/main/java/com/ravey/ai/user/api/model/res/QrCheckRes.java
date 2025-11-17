package com.ravey.ai.user.api.model.res;

import lombok.Data;

/**
 * @author ravey
 * @since 1.0.0
 */
@Data
public class QrCheckRes {
    private Integer status;
    private String token;
    private MiniProgramLoginRes.UserInfo userInfo;
}