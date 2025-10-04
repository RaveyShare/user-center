package com.ravey.ai.user.web.controller.front;

import com.ravey.ai.user.api.model.req.MiniProgramLoginReq;
import com.ravey.ai.user.api.model.res.MiniProgramLoginRes;
import com.ravey.ai.user.api.service.AuthService;
import com.ravey.common.service.web.result.HttpResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.net.http.HttpResponse;

/**
 * 前端认证控制器
 *
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/front/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * 小程序登录
     *
     * @param req 登录请求
     * @return 登录响应
     */
    @PostMapping("/miniProgram/login")
    public HttpResult<MiniProgramLoginRes> miniProgramLogin(@Valid @RequestBody MiniProgramLoginReq req) {
        log.info("收到小程序登录请求: appId={}", req.getAppId());
        MiniProgramLoginRes response = authService.miniProgramLogin(req);
        log.info("小程序登录成功: appId={}, userId={}", req.getAppId(), response.getUserInfo().getId());
        return HttpResult.success(response);
    }
}