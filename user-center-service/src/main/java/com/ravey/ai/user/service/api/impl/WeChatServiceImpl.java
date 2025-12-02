package com.ravey.ai.user.service.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ravey.ai.user.api.dto.AppsDTO;
import com.ravey.ai.user.api.model.dto.WeChatSessionDto;
import com.ravey.ai.user.service.cache.CacheService;
import com.ravey.ai.user.service.converter.AppsConverter;
import com.ravey.ai.user.service.dao.entity.Apps;
import com.ravey.ai.user.service.dao.mapper.AppsMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import me.chanjar.weixin.common.error.WxErrorException;
import cn.binarywang.wx.miniapp.bean.WxMaCodeLineColor;

/**
 * 微信API服务实现类
 *
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatServiceImpl {

    private final AppsMapper appsMapper;
    private final CacheService cacheService;

    @Value("${wechat.api.base-url}")
    private String wechatApiBaseUrl;

    @Value("${wechat.api.session-url}")
    private String wechatSessionUrl;

    @Value("${wechat.api.token-url}")
    private String wechatTokenUrl;



    /**
     * 获取微信小程序 Access Token（带缓存）
     *
     * @param appId 应用ID
     * @return Access Token
     */
    public String getMiniAppAccessToken(String appId) {
        try {
            // 先从缓存获取
            String cachedToken = cacheService.getMiniAppAccessToken(appId);
            if (StringUtils.hasText(cachedToken)) {
                log.debug("从缓存获取微信AccessToken: appId={}", appId);
                return cachedToken;
            }

            // 缓存中没有，调用微信API获取
            log.info("从微信API获取AccessToken: appId={}", appId);
            
            // 根据appId查询应用信息
            Apps app = getAppByAppId(appId);
            if (app == null) {
                log.error("应用不存在或已禁用: {}", appId);
                return null;
            }

            WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
            config.setAppid(app.getAppId());
            config.setSecret(app.getAppSecret());
            WxMaService wxService = new WxMaServiceImpl();
            wxService.setWxMaConfig(config);
            String accessToken = wxService.getAccessToken();
            if (StringUtils.hasText(accessToken)) {
                cacheService.cacheMiniAppAccessToken(appId, accessToken);
                log.info("获取微信AccessToken成功并缓存: appId={}", appId);
                return accessToken;
            } else {
                log.error("获取微信AccessToken失败: appId={}", appId);
                return null;
            }

        } catch (Exception e) {
            log.error("获取微信AccessToken异常: appId={}", appId, e);
            return null;
        }
    }

    /**
     * 获取微信会话信息（优化版本，添加缓存检查）
     *
     * @param appId 应用ID
     * @param code  微信授权码
     * @return 微信会话信息
     */
    public WeChatSessionDto getWeChatSession(String appId, String code) {
        try {
            // 检查微信授权码是否已使用（防重复）
            Boolean codeUsed = cacheService.isWeChatCodeUsed(appId, code);
            if (Boolean.TRUE.equals(codeUsed)) {
                log.warn("微信授权码已使用: appId={}, code={}", appId, code);
                WeChatSessionDto errorResult = new WeChatSessionDto();
                errorResult.setErrcode(-1);
                errorResult.setErrmsg("授权码已使用");
                return errorResult;
            }

            // 根据appId查询应用信息（优先从缓存获取）
            Apps app = getAppByAppId(appId);
            if (app == null) {
                log.error("应用不存在或已禁用: {}", appId);
                WeChatSessionDto errorResult = new WeChatSessionDto();
                errorResult.setErrcode(-1);
                errorResult.setErrmsg("应用不存在或已禁用");
                return errorResult;
            }

            log.info("调用微信登录API: appId={}", appId);
            WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
            config.setAppid(app.getAppId());
            config.setSecret(app.getAppSecret());
            WxMaService wxService = new WxMaServiceImpl();
            wxService.setWxMaConfig(config);
            WxMaJscode2SessionResult session = wxService.jsCode2SessionInfo(code);
            WeChatSessionDto result = new WeChatSessionDto();
            result.setOpenid(session.getOpenid());
            result.setSessionKey(session.getSessionKey());
            result.setUnionid(session.getUnionid());
            result.setErrcode(0);
            result.setErrmsg(null);
            log.info("微信API调用成功: openid={}", result.getOpenid());
            cacheService.cacheWeChatSession(appId, code, true);
            return result;

        } catch (Exception e) {
            log.error("调用微信API异常", e);
            WeChatSessionDto errorResult = new WeChatSessionDto();
            errorResult.setErrcode(-1);
            errorResult.setErrmsg("调用微信API异常: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 根据appId获取应用信息（优先从缓存获取）
     *
     * @param appId 应用ID
     * @return 应用信息
     */
    private Apps getAppByAppId(String appId) {
        // 先从缓存获取
        AppsDTO cachedApp = cacheService.getAppInfo(appId);
        if (cachedApp != null) {
            log.debug("从缓存获取应用信息: appId={}", appId);
            // 转换API层AppsDTO为Service层Apps
            return AppsConverter.toEntity(cachedApp);
        }

        // 缓存中没有，从数据库获取
        Apps app = appsMapper.selectOne(
                new LambdaQueryWrapper<Apps>()
                        .eq(Apps::getAppId, appId)
                        .eq(Apps::getStatus, 1)
        );

        if (app != null) {
            // 缓存应用信息 - 转换Service层Apps为API层AppsDTO
            cacheService.cacheAppInfo(AppsConverter.toDTO(app));
            log.debug("从数据库获取应用信息并缓存: appId={}", appId);
        }

        return app;
    }

    /**
     * 微信AccessToken响应类
     */
    @Data
    public static class WeChatAccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private Integer expiresIn;

        private Integer errcode;
        private String errmsg;
    }
    /**
     * 获取小程序码（无限制版）
     */
    public byte[] getWxaCodeUnlimited(String appId, String scene, String page, Integer width, String envVersion, Boolean checkPath, Boolean hyaline) {
        try {
            String accessToken = getMiniAppAccessToken(appId);
            if (!StringUtils.hasText(accessToken)) {
                return null;
            }

            Apps app = getAppByAppId(appId);
            if (app == null) {
                log.error("应用不存在或已禁用: {}", appId);
                return null;
            }

            WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
            config.setAppid(app.getAppId());
            config.setSecret(app.getAppSecret());

            WxMaService wxService = new WxMaServiceImpl();
            wxService.setWxMaConfig(config);

            try {
                int w = (width != null && width > 0) ? width : 430;
                String env = (envVersion != null && envVersion.length() > 0) ? envVersion : "release";
                boolean chk = (checkPath == null) ? true : checkPath;
                boolean hy = (hyaline == null) ? false : hyaline;
                java.io.File file = wxService.getQrcodeService().createWxaCodeUnlimit(
                        scene,
                        page,
                        hy,          // isHyaline
                        env,         // envVersion
                        w,           // width
                        true,        // autoColor
                        (WxMaCodeLineColor) null,
                        chk          // checkPath
                );
                byte[] result = java.nio.file.Files.readAllBytes(file.toPath());
                try { file.delete(); } catch (Exception ignore) {}
                return result;
            } catch (WxErrorException ex) {
                log.error("获取小程序码失败: appId={}, scene={}, errcode={}, errmsg={}", appId, scene, ex.getError().getErrorCode(), ex.getError().getErrorMsg());
                return null;
            }
        } catch (Exception e) {
            log.error("获取小程序码失败: appId={}, scene={}", appId, scene, e);
            return null;
        }
    }

    private String joinUrl(String base, String path) {
        String b = base == null ? "" : (base.endsWith("/") ? base.substring(0, base.length() - 1) : base);
        String p = path == null ? "" : (path.startsWith("/") ? path.substring(1) : path);
        return b + "/" + p;
    }
}
