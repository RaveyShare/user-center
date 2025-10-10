package com.ravey.ai.user.service.converter;

import com.ravey.ai.user.api.dto.AppsDTO;
import com.ravey.ai.user.service.dao.entity.Apps;

/**
 * 应用实体转换器
 * 用于Entity和DTO之间的转换
 *
 * @author ravey
 * @since 1.0.0
 */
public class AppsConverter {

    /**
     * Entity转DTO
     *
     * @param entity 实体对象
     * @return DTO对象
     */
    public static AppsDTO toDTO(Apps entity) {
        if (entity == null) {
            return null;
        }
        
        AppsDTO dto = new AppsDTO();
        dto.setId(entity.getId());
        dto.setAppId(entity.getAppId());
        dto.setAppName(entity.getAppName());
        dto.setAppSecret(entity.getAppSecret());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        
        return dto;
    }

    /**
     * DTO转Entity
     *
     * @param dto DTO对象
     * @return 实体对象
     */
    public static Apps toEntity(AppsDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Apps entity = new Apps();
        entity.setId(dto.getId());
        entity.setAppId(dto.getAppId());
        entity.setAppName(dto.getAppName());
        entity.setAppSecret(dto.getAppSecret());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        
        return entity;
    }
}