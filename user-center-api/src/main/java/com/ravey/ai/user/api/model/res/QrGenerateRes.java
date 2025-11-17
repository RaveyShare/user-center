package com.ravey.ai.user.api.model.res;

import lombok.Data;

/**
 * @author ravey
 * @since 1.0.0
 */
@Data
public class QrGenerateRes {
    private String qrcodeId;
    private Long expireAt;
    private String qrContent;
}