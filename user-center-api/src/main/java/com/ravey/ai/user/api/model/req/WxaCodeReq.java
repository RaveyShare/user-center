package com.ravey.ai.user.api.model.req;

import lombok.Data;

/**
 * @author Ravey
 * @since 1.0.0
 */
@Data
public class WxaCodeReq {
    private String appId;
    private String qrcodeId;
    private String page;
    private Integer width;
    private String envVersion;
    private Boolean checkPath;
    private Boolean hyaline;
}
