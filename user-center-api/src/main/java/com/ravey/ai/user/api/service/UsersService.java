package com.ravey.ai.user.api.service;

import com.ravey.ai.user.api.dto.UsersDTO;
import com.ravey.ai.user.api.model.req.UserUpdateReq;

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

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param req 更新请求
     * @return 更新后的用户信息
     */
    UsersDTO update(Long userId, UserUpdateReq req);
}