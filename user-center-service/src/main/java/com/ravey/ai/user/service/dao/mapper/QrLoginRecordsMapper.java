package com.ravey.ai.user.service.dao.mapper;

import com.ravey.ai.user.service.dao.entity.QrLoginRecords;
import com.ravey.common.dao.mp.mapper.BaseBizMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 二维码登录记录表Mapper接口
 *
 * @author ravey
 * @since 1.0.0
 */
@Mapper
public interface QrLoginRecordsMapper extends BaseBizMapper<QrLoginRecords> {

}