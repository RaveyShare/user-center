package com.ravey.ai.user.service.dao.mapper;

import com.ravey.ai.user.service.dao.entity.Apps;
import com.ravey.common.dao.mp.mapper.BaseBizMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用表Mapper接口
 *
 * @author ravey
 * @since 1.0.0
 */
@Mapper
public interface AppsMapper extends BaseBizMapper<Apps> {

}