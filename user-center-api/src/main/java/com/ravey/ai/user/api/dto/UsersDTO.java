package com.ravey.ai.user.api.dto;

import lombok.Data;

/**
 * 用户数据传输对象（DTO）
 * 用于API层的数据传输，只包含API层需要的字段
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
public class UsersDTO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;
}