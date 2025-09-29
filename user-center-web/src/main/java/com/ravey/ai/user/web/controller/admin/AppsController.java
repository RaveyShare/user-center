package com.ravey.ai.user.web.controller.admin;

import com.ravey.ai.user.api.model.req.AppCreateReq;
import com.ravey.ai.user.api.model.res.AppListRes;
import com.ravey.ai.user.api.model.res.AppRes;
import com.ravey.ai.user.api.service.AppsService;
import com.ravey.common.service.web.result.HttpResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 应用管理控制器
 *
 * @author ravey
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin/apps")
@RequiredArgsConstructor
@Tag(name = "应用管理", description = "应用管理相关接口")
public class AppsController {

    private final AppsService appsService;

    /**
     * 创建应用
     *
     * @param req 创建应用请求
     * @return 应用信息
     */
    @PostMapping("createApp")
    @Operation(summary = "创建应用", description = "创建新的应用")
    public HttpResult<AppRes> createApp(@Valid @RequestBody AppCreateReq req) {
        AppRes appRes = appsService.createApp(req);
        return HttpResult.success(appRes);
    }

    /**
     * 获取应用列表
     *
     * @return 应用列表
     */
    @GetMapping("/getAppList")
    @Operation(summary = "获取应用列表", description = "获取所有应用的列表")
    public HttpResult<AppListRes> getAppList() {
        AppListRes appListRes = appsService.getAppList();
        return HttpResult.success(appListRes);
    }
}