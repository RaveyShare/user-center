package com.ravey.ai.user.api.model.res;

import lombok.Data;

/**
 * @author Ravey
 * @since 1.0.0
 */
@Data
public class WxaCodeRes {
    private String qrcodeId;
    private Long expireAt;
    private String imageBase64;
}