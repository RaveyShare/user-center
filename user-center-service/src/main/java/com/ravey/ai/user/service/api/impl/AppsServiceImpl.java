package com.ravey.ai.user.service.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ravey.ai.user.api.model.req.AppCreateReq;
import com.ravey.ai.user.api.model.res.AppListRes;
import com.ravey.ai.user.api.model.res.AppRes;
import com.ravey.ai.user.api.service.AppsService;
import com.ravey.ai.user.service.dao.entity.Apps;
import com.ravey.ai.user.service.dao.mapper.AppsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用服务实现类
 *
 * @author ravey
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AppsServiceImpl implements AppsService {

    private final AppsMapper appsMapper;

    @Override
    public AppRes createApp(AppCreateReq req) {
        // 检查appId是否已存在
        LambdaQueryWrapper<Apps> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Apps::getAppId, req.getAppId());
        Apps existingApp = appsMapper.selectOne(queryWrapper);
        if (existingApp != null) {
            throw new RuntimeException("应用AppId已存在");
        }

        // 创建应用实体
        Apps app = new Apps();
        BeanUtils.copyProperties(req, app);
        app.setStatus(1); // 默认启用状态

        // 保存到数据库
        appsMapper.insert(app);

        // 转换为响应DTO
        AppRes appRes = new AppRes();
        BeanUtils.copyProperties(app, appRes);
        return appRes;
    }

    @Override
    public AppListRes getAppList() {
        // 查询所有应用
        List<Apps> appsList = appsMapper.selectList(null);

        // 转换为响应DTO
        List<AppRes> appResList = appsList.stream()
                .map(app -> {
                    AppRes appRes = new AppRes();
                    BeanUtils.copyProperties(app, appRes);
                    return appRes;
                })
                .collect(Collectors.toList());

        // 构建列表响应
        AppListRes appListRes = new AppListRes();
        appListRes.setList(appResList);
        appListRes.setTotal((long) appResList.size());
        return appListRes;
    }
}