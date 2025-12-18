package com.ravey.ai.user.web.controller.front;

import com.ravey.ai.user.api.model.req.*;
import com.ravey.ai.user.api.model.res.*;
import com.ravey.ai.user.api.service.AuthService;
import com.ravey.ai.user.api.service.UsersService;
import com.ravey.common.service.web.result.HttpResult;
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

    @Resource
    private UsersService usersService;

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

    @PostMapping("/email/sendCode")
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送验证码")
    public HttpResult<Void> sendEmailCode(@RequestBody EmailSendCodeReq req) {
        usersService.sendEmailCode(req);
        return HttpResult.success(null);
    }

    @PostMapping("/email/register")
    @Operation(summary = "邮箱注册", description = "通过邮箱和验证码注册新用户")
    public HttpResult<LoginRes> registerByEmail(@RequestBody EmailRegisterReq req) {
        return HttpResult.success(usersService.registerByEmail(req));
    }

    @PostMapping("/email/login")
    @Operation(summary = "邮箱登录", description = "支持密码登录或验证码登录")
    public HttpResult<LoginRes> loginByEmail(@RequestBody EmailLoginReq req) {
        return HttpResult.success(usersService.loginByEmail(req));
    }

    @PostMapping("/email/resetPassword")
    @Operation(summary = "重置密码", description = "通过验证码验证后重置密码")
    public HttpResult<Void> resetPassword(@RequestBody PasswordResetReq req) {
        usersService.resetPassword(req);
        return HttpResult.success(null);
    }

}