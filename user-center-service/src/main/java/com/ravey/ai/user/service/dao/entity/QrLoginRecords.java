package com.ravey.ai.user.service.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ravey.common.dao.mp.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 二维码登录记录表实体类
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qr_login_records")
public class QrLoginRecords extends BaseEntity {

    /**
     * 二维码唯一标识
     */
    @TableField("qrcode_id")
    private String qrcodeId;

    /**
     * 应用ID（扫码时填入）
     */
    @TableField("app_id")
    private Long appId;

    /**
     * 用户ID（扫码确认后填入）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 微信小程序openId
     */
    @TableField("openid")
    private String openid;

    /**
     * 状态：0-待扫码，1-已扫码待确认，2-已确认，3-已过期
     */
    @TableField("status")
    private Integer status;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}