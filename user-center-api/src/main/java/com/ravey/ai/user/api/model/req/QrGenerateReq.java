package com.ravey.ai.user.api.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author ravey
 * @since 1.0.0
 */
@Data
public class QrGenerateReq {
    @NotBlank(message = "应用ID不能为空")
    private String appId;
    private String scene;
}