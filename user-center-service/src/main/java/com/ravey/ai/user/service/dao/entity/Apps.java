package com.ravey.ai.user.service.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ravey.common.dao.mp.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用表实体类
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("apps")
public class Apps extends BaseEntity {

    /**
     * 应用名称
     */
    @TableField("app_name")
    private String appName;

    /**
     * 微信小程序AppId
     */
    @TableField("app_id")
    private String appId;

    /**
     * 微信小程序AppSecret
     */
    @TableField("app_secret")
    private String appSecret;

    /**
     * 应用描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态：1-启用，0-禁用
     */
    @TableField("status")
    private Integer status;
}