package com.ravey.ai.user.api.service;

import com.ravey.ai.user.api.model.req.AppCreateReq;
import com.ravey.ai.user.api.model.res.AppListRes;
import com.ravey.ai.user.api.model.res.AppRes;

/**
 * 应用服务接口
 *
 * @author ravey
 * @since 1.0.0
 */
public interface AppsService {

    /**
     * 创建应用
     *
     * @param req 创建应用请求
     * @return 应用信息
     */
    AppRes createApp(AppCreateReq req);

    /**
     * 获取应用列表
     *
     * @return 应用列表
     */
    AppListRes getAppList();
}