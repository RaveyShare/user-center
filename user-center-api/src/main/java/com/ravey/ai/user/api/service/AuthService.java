package com.ravey.ai.user.api.service;

import com.ravey.ai.user.api.model.req.MiniProgramLoginReq;
import com.ravey.ai.user.api.model.res.MiniProgramLoginRes;
import com.ravey.ai.user.api.model.req.QrGenerateReq;
import com.ravey.ai.user.api.model.req.QrCheckReq;
import com.ravey.ai.user.api.model.req.QrScanReq;
import com.ravey.ai.user.api.model.req.QrConfirmReq;
import com.ravey.ai.user.api.model.res.QrGenerateRes;
import com.ravey.ai.user.api.model.res.QrCheckRes;

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

    /**
     * 生成扫码登录二维码
     */
    QrGenerateRes generateQr(QrGenerateReq req);

    /**
     * 查询二维码状态
     */
    QrCheckRes checkQr(QrCheckReq req);

    /**
     * 小程序扫码上报
     */
    void scanQr(QrScanReq req);

    /**
     * 小程序确认登录
     */
    void confirmQr(QrConfirmReq req);

    /**
     * 生成小程序码（携带二维码登录场景值）
     */
    com.ravey.ai.user.api.model.res.WxaCodeRes generateWxaCode(com.ravey.ai.user.api.model.req.WxaCodeReq req);
}