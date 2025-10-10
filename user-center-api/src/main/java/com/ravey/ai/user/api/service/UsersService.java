package com.ravey.ai.user.api.service;

import com.ravey.ai.user.api.dto.UsersDTO;

/**
 * 用户服务接口
 *
 * @author ravey
 * @since 1.0.0
 */
public interface UsersService {
    
    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UsersDTO getById(Long userId);
}