package com.ravey.ai.user.service.dao.mapper;

import com.ravey.ai.user.service.dao.entity.Users;
import com.ravey.common.dao.mp.mapper.BaseBizMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表Mapper接口
 *
 * @author ravey
 * @since 1.0.0
 */
@Mapper
public interface UsersMapper extends BaseBizMapper<Users> {

}