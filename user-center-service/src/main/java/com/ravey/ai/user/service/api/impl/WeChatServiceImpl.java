package com.ravey.ai.user.service.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ravey.ai.user.api.model.dto.WeChatSessionDto;
import com.ravey.ai.user.service.dao.entity.Apps;
import com.ravey.ai.user.service.dao.mapper.AppsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

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

    private final RestTemplate restTemplate;
    private final AppsMapper appsMapper;

    @Value("${wechat.api.base-url}")
    private String wechatApiBaseUrl;

    @Value("${wechat.api.session-url}")
    private String wechatSessionUrl;



    /**
     * 获取微信会话信息
     *
     * @param appId 应用ID
     * @param code  微信授权码
     * @return 微信会话信息
     */
    public WeChatSessionDto getWeChatSession(String appId, String code) {
        try {
            // 根据appId查询应用信息
            Apps app = appsMapper.selectOne(
                    new LambdaQueryWrapper<Apps>()
                            .eq(Apps::getAppId, appId)
                            .eq(Apps::getStatus, 1)
            );

            if (app == null) {
                log.error("应用不存在或已禁用: {}", appId);
                WeChatSessionDto errorResult = new WeChatSessionDto();
                errorResult.setErrcode(-1);
                errorResult.setErrmsg("应用不存在或已禁用");
                return errorResult;
            }

            // 构建请求URL
            String url = String.format("%s%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wechatApiBaseUrl, wechatSessionUrl, app.getAppId(), app.getAppSecret(), code);

            log.info("调用微信登录API: appId={}", appId);

            // 调用微信API
            WeChatSessionDto result = restTemplate.getForObject(url, WeChatSessionDto.class);

            if (result != null && result.getErrcode() != null && result.getErrcode() != 0) {
                log.error("微信API调用失败: errcode={}, errmsg={}", result.getErrcode(), result.getErrmsg());
            } else {
                log.info("微信API调用成功: openid={}", result != null ? result.getOpenid() : "null");
            }

            return result;

        } catch (Exception e) {
            log.error("调用微信API异常", e);
            WeChatSessionDto errorResult = new WeChatSessionDto();
            errorResult.setErrcode(-1);
            errorResult.setErrmsg("调用微信API异常: " + e.getMessage());
            return errorResult;
        }
    }
}