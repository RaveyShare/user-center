package com.ravey.ai.user.api.model.req;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建应用请求DTO
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
public class AppCreateReq {

    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 100, message = "应用名称长度不能超过100个字符")
    private String appName;

    /**
     * 微信小程序AppId
     */
    @NotBlank(message = "微信小程序AppId不能为空")
    @Size(max = 100, message = "微信小程序AppId长度不能超过100个字符")
    private String appId;

    /**
     * 微信小程序AppSecret
     */
    @NotBlank(message = "微信小程序AppSecret不能为空")
    @Size(max = 255, message = "微信小程序AppSecret长度不能超过255个字符")
    private String appSecret;

    /**
     * 应用描述
     */
    @Size(max = 500, message = "应用描述长度不能超过500个字符")
    private String description;
}