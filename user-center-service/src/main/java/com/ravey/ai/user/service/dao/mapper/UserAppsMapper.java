package com.ravey.ai.user.service.dao.mapper;

import com.ravey.ai.user.service.dao.entity.UserApps;
import com.ravey.common.dao.mp.mapper.BaseBizMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户应用关联表Mapper接口
 *
 * @author ravey
 * @since 1.0.0
 */
@Mapper
public interface UserAppsMapper extends BaseBizMapper<UserApps> {

}