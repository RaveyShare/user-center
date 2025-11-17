package com.ravey.ai.user.api.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author ravey
 * @since 1.0.0
 */
@Data
public class QrCheckReq {
    @NotBlank(message = "二维码ID不能为空")
    private String qrcodeId;
}