package com.ravey.ai.user.web.controller.front;

import com.ravey.ai.user.api.model.req.MiniProgramLoginReq;
import com.ravey.ai.user.api.model.res.MiniProgramLoginRes;
import com.ravey.ai.user.api.service.AuthService;
import com.ravey.common.service.web.result.HttpResult;
import com.ravey.ai.user.api.model.req.QrGenerateReq;
import com.ravey.ai.user.api.model.req.QrCheckReq;
import com.ravey.ai.user.api.model.req.QrScanReq;
import com.ravey.ai.user.api.model.req.QrConfirmReq;
import com.ravey.ai.user.api.model.res.QrGenerateRes;
import com.ravey.ai.user.api.model.res.QrCheckRes;
import com.ravey.ai.user.api.model.req.WxaCodeReq;
import com.ravey.ai.user.api.model.res.WxaCodeRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 认证控制器
 * 
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/front/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 微信小程序登录
     * 
     * @param request 微信小程序登录请求
     * @return 访问令牌信息
     */
    @PostMapping("/wxMiniAppLogin")
    @Operation(summary = "微信小程序登录", description = "通过微信小程序登录凭证进行用户登录")
    public HttpResult<MiniProgramLoginRes> wxMiniAppLogin(@RequestBody MiniProgramLoginReq request) {
        log.info("微信小程序登录请求: appId={}", request.getAppId());
        
        try {
            // 调用服务层进行登录
            MiniProgramLoginRes serviceRes = authService.miniProgramLogin(request);
            
            // 设置令牌过期时间
            serviceRes.setExpiresIn(86400L); // 24小时
            
            log.info("微信小程序登录成功: appId={}, userId={}", 
                    request.getAppId(), 
                    serviceRes.getUserInfo() != null ? serviceRes.getUserInfo().getId() : null);
            
            return HttpResult.success(serviceRes);
            
        } catch (Exception e) {
            log.error("微信小程序登录失败: appId={}, error={}", request.getAppId(), e.getMessage(), e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/qr/generate")
    @Operation(summary = "生成扫码登录二维码", description = "网页端生成二维码，用于小程序扫码登录")
    public HttpResult<QrGenerateRes> generateQr(@RequestBody QrGenerateReq request) {
        QrGenerateRes res = authService.generateQr(request);
        return HttpResult.success(res);
    }

    @PostMapping("/qr/check")
    @Operation(summary = "查询二维码状态", description = "网页端轮询二维码状态，确认后返回token")
    public HttpResult<QrCheckRes> checkQr(@RequestBody QrCheckReq request) {
        QrCheckRes res = authService.checkQr(request);
        return HttpResult.success(res);
    }

    @PostMapping("/qr/scan")
    @Operation(summary = "小程序扫码上报", description = "小程序扫码后上报二维码状态")
    public HttpResult<Void> scanQr(@RequestBody QrScanReq request) {
        authService.scanQr(request);
        return HttpResult.success(null);
    }

    @PostMapping("/qr/confirm")
    @Operation(summary = "小程序确认登录", description = "小程序在用户确认后提交登录确认")
    public HttpResult<Void> confirmQr(@RequestBody QrConfirmReq request) {
        authService.confirmQr(request);
        return HttpResult.success(null);
    }

    @PostMapping("/qr/wxacode")
    @Operation(summary = "生成小程序码", description = "生成携带登录场景值的小程序码")
    public HttpResult<WxaCodeRes> generateWxacode(@RequestBody WxaCodeReq request) {
        WxaCodeRes res = authService.generateWxaCode(request);
        return HttpResult.success(res);
    }


}