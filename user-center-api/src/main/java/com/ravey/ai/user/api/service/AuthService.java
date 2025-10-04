package com.ravey.ai.user.api.service;

import com.ravey.ai.user.api.model.req.MiniProgramLoginReq;
import com.ravey.ai.user.api.model.res.MiniProgramLoginRes;

/**
 * 认证服务接口
 *
 * @author ravey
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 小程序登录
     *
     * @param req 登录请求
     * @return 登录响应
     */
    MiniProgramLoginRes miniProgramLogin(MiniProgramLoginReq req);
}