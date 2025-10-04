package com.ravey.ai.user.service.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ravey.common.dao.mp.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户应用关联表实体类
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_apps")
public class UserApps extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 应用ID
     */
    @TableField("app_id")
    private Long appId;

    /**
     * 微信小程序openId
     */
    @TableField("openid")
    private String openid;

    /**
     * 微信unionId
     */
    @TableField("unionid")
    private String unionid;

    /**
     * 关联状态：1-正常，0-禁用
     */
    @TableField("status")
    private Integer status;
}