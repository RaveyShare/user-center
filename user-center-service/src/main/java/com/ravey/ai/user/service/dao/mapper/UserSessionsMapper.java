package com.ravey.ai.user.service.dao.mapper;

import com.ravey.ai.user.service.dao.entity.UserSessions;
import com.ravey.common.dao.mp.mapper.BaseBizMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户会话表Mapper接口
 *
 * @author ravey
 * @since 1.0.0
 */
@Mapper
public interface UserSessionsMapper extends BaseBizMapper<UserSessions> {

}