package com.ravey.ai.user.service.api.impl;

import com.ravey.ai.user.api.dto.UsersDTO;
import com.ravey.ai.user.api.service.UsersService;
import com.ravey.ai.user.service.converter.UsersConverter;
import com.ravey.ai.user.service.dao.entity.Users;
import com.ravey.ai.user.service.dao.mapper.UsersMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author Ravey
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersMapper usersMapper;

    @Override
    public UsersDTO getById(Long userId) {
        log.debug("根据ID获取用户信息: userId={}", userId);
        
        if (userId == null) {
            log.warn("用户ID不能为空");
            return null;
        }
        
        try {
            // 通过Mapper查询用户实体
            Users user = usersMapper.selectById(userId);
            
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                return null;
            }
            
            // 转换为DTO并返回
            UsersDTO userDTO = UsersConverter.toDTO(user);
            log.debug("成功获取用户信息: userId={}, nickname={}", userId, userDTO.getNickname());
            
            return userDTO;
            
        } catch (Exception e) {
            log.error("获取用户信息失败: userId={}", userId, e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }
}