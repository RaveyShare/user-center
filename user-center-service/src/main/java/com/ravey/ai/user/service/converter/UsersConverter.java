package com.ravey.ai.user.service.converter;

import com.ravey.ai.user.api.dto.UsersDTO;
import com.ravey.ai.user.service.dao.entity.Users;

/**
 * 用户实体转换器
 * 用于Entity和DTO之间的转换
 *
 * @author ravey
 * @since 1.0.0
 */
public class UsersConverter {

    /**
     * Entity转DTO
     *
     * @param entity 实体对象
     * @return DTO对象
     */
    public static UsersDTO toDTO(Users entity) {
        if (entity == null) {
            return null;
        }
        
        UsersDTO dto = new UsersDTO();
        dto.setId(entity.getId());
        dto.setNickname(entity.getNickname());
        dto.setAvatar(entity.getAvatarUrl());
        dto.setStatus(entity.getStatus());
        
        return dto;
    }

    /**
     * DTO转Entity
     *
     * @param dto DTO对象
     * @return 实体对象
     */
    public static Users toEntity(UsersDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Users entity = new Users();
        entity.setId(dto.getId());
        entity.setNickname(dto.getNickname());
        entity.setAvatarUrl(dto.getAvatar());
        entity.setStatus(dto.getStatus());
        
        return entity;
    }
}