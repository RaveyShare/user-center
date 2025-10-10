package com.ravey.ai.user.service.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ravey.ai.user.api.model.dto.WeChatSessionDto;
import com.ravey.ai.user.api.model.req.MiniProgramLoginReq;
import com.ravey.ai.user.api.model.res.MiniProgramLoginRes;
import com.ravey.ai.user.api.service.AuthService;
import com.ravey.ai.user.service.cache.CacheService;
import com.ravey.ai.user.api.utils.JwtUtils;
import com.ravey.ai.user.service.converter.UsersConverter;
import com.ravey.ai.user.service.converter.AppsConverter;
import com.ravey.ai.user.service.dao.entity.Apps;
import com.ravey.ai.user.service.dao.entity.UserApps;
import com.ravey.ai.user.service.dao.entity.UserSessions;
import com.ravey.ai.user.service.dao.entity.Users;
import com.ravey.ai.user.service.dao.mapper.AppsMapper;
import com.ravey.ai.user.service.dao.mapper.UserAppsMapper;
import com.ravey.ai.user.service.dao.mapper.UserSessionsMapper;
import com.ravey.ai.user.service.dao.mapper.UsersMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 *
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final WeChatServiceImpl weChatService;
    private final JwtUtils jwtUtils;
    private final CacheService cacheService;
    private final AppsMapper appsMapper;
    private final UsersMapper usersMapper;
    private final UserAppsMapper userAppsMapper;
    private final UserSessionsMapper userSessionsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniProgramLoginRes miniProgramLogin(MiniProgramLoginReq req) {
        log.info("小程序登录开始: appId={}", req.getAppId());

        try {
            // 1. 调用微信API获取openid和unionid
            WeChatSessionDto weChatSession = weChatService.getWeChatSession(req.getAppId(), req.getCode());
            
            if (weChatSession == null || weChatSession.getErrcode() != null && weChatSession.getErrcode() != 0) {
                throw new RuntimeException("微信登录失败: " + (weChatSession != null ? weChatSession.getErrmsg() : "未知错误"));
            }

            // 2. 获取应用信息（优先从缓存获取）
            Apps app = getAppByAppId(req.getAppId());
            if (app == null) {
                throw new RuntimeException("应用不存在或已禁用");
            }

            // 3. 查找或创建用户
            Users user = findOrCreateUser(weChatSession, req.getUserInfo());

            // 4. 查找或创建用户应用关联
            UserApps userApp = findOrCreateUserApp(user.getId(), app.getId(), weChatSession);

            // 5. 生成JWT令牌
            String token = jwtUtils.generateToken(user.getId(), app.getAppId());

            // 6. 创建会话记录和缓存
            createUserSession(user.getId(), app.getId(), token);
            cacheService.cacheUserSession(token, user.getId());
            
            // 7. 缓存用户信息 - 转换Service层Users为API层UsersDTO
            cacheService.cacheUserInfo(UsersConverter.toDTO(user));

            // 8. 构建响应
            MiniProgramLoginRes response = new MiniProgramLoginRes();
            response.setToken(token);

            MiniProgramLoginRes.UserInfo userInfo = new MiniProgramLoginRes.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setNickname(user.getNickname());
            userInfo.setAvatarUrl(user.getAvatarUrl());
            response.setUserInfo(userInfo);

            log.info("小程序登录成功: userId={}, appId={}", user.getId(), app.getId());
            return response;

        } catch (Exception e) {
            log.error("小程序登录失败", e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 查找或创建用户
     */
    private Users findOrCreateUser(WeChatSessionDto weChatSession, MiniProgramLoginReq.UserInfo userInfo) {
        Users user = null;

        // 如果有unionid，先通过unionid查找用户
        if (StringUtils.hasText(weChatSession.getUnionid())) {
            UserApps existingUserApp = userAppsMapper.selectOne(
                    new LambdaQueryWrapper<UserApps>()
                            .eq(UserApps::getUnionid, weChatSession.getUnionid())
                            .eq(UserApps::getStatus, 1)
                            .last("LIMIT 1")
            );

            if (existingUserApp != null) {
                user = usersMapper.selectById(existingUserApp.getUserId());
            }
        }

        // 如果没有找到用户，创建新用户
        if (user == null) {
            user = new Users();
            if (userInfo != null) {
                user.setNickname(userInfo.getNickname());
                user.setAvatarUrl(userInfo.getAvatarUrl());
            }
            user.setStatus(1);
            usersMapper.insert(user);
            log.info("创建新用户: userId={}", user.getId());
        } else {
            // 更新用户信息（如果提供了新的用户信息）
            if (userInfo != null) {
                boolean needUpdate = false;
                if (StringUtils.hasText(userInfo.getNickname()) && !userInfo.getNickname().equals(user.getNickname())) {
                    user.setNickname(userInfo.getNickname());
                    needUpdate = true;
                }
                if (StringUtils.hasText(userInfo.getAvatarUrl()) && !userInfo.getAvatarUrl().equals(user.getAvatarUrl())) {
                    user.setAvatarUrl(userInfo.getAvatarUrl());
                    needUpdate = true;
                }
                if (needUpdate) {
                    usersMapper.updateById(user);
                    log.info("更新用户信息: userId={}", user.getId());
                }
            }
        }

        return user;
    }

    /**
     * 查找或创建用户应用关联（优化版本，添加缓存检查）
     */
    private UserApps findOrCreateUserApp(Long userId, Long appId, WeChatSessionDto weChatSession) {
        // 先检查缓存中的用户应用关联
        Boolean cachedExists = cacheService.getUserApp(userId, String.valueOf(appId));
        
        // 查找现有关联
        UserApps userApp = userAppsMapper.selectOne(
                new LambdaQueryWrapper<UserApps>()
                        .eq(UserApps::getUserId, userId)
                        .eq(UserApps::getAppId, appId)
        );

        if (userApp == null) {
            // 创建新的用户应用关联
            userApp = new UserApps();
            userApp.setUserId(userId);
            userApp.setAppId(appId);
            userApp.setOpenid(weChatSession.getOpenid());
            userApp.setUnionid(weChatSession.getUnionid());
            userApp.setStatus(1);
            userAppsMapper.insert(userApp);
            
            // 缓存用户应用关联
            cacheService.cacheUserApp(userId, String.valueOf(appId), true);
            
            log.info("创建用户应用关联: userId={}, appId={}", userId, appId);
        } else {
            // 更新openid和unionid（可能会变化）
            boolean needUpdate = false;
            if (!weChatSession.getOpenid().equals(userApp.getOpenid())) {
                userApp.setOpenid(weChatSession.getOpenid());
                needUpdate = true;
            }
            if (StringUtils.hasText(weChatSession.getUnionid()) && !weChatSession.getUnionid().equals(userApp.getUnionid())) {
                userApp.setUnionid(weChatSession.getUnionid());
                needUpdate = true;
            }
            if (needUpdate) {
                userAppsMapper.updateById(userApp);
                log.info("更新用户应用关联: userId={}, appId={}", userId, appId);
            }
            
            // 如果关联已存在，也缓存一下
            cacheService.cacheUserApp(userId, String.valueOf(appId), true);
        }

        return userApp;
    }

    /**
     * 创建用户会话
     */
    private void createUserSession(Long userId, Long appId, String token) {
        UserSessions session = new UserSessions();
        session.setUserId(userId);
        session.setAppId(appId);
        session.setSessionToken(token);
        session.setExpireTime(LocalDateTime.now().plusHours(24)); // 24小时过期
        userSessionsMapper.insert(session);
        log.info("创建用户会话: userId={}, appId={}", userId, appId);
    }

    /**
     * 获取应用信息（优先从缓存获取）
     */
    private Apps getAppByAppId(String appId) {
        // 先从缓存获取
        com.ravey.ai.user.api.dto.AppsDTO cachedApp = cacheService.getAppInfo(appId);
        if (cachedApp != null) {
            log.debug("从缓存获取应用信息成功，appId: {}", appId);
            // 转换API层AppsDTO为Service层Apps
            return AppsConverter.toEntity(cachedApp);
        }

        // 缓存中没有，从数据库查询
        Apps app = appsMapper.selectOne(
                new LambdaQueryWrapper<Apps>()
                        .eq(Apps::getAppId, appId)
                        .eq(Apps::getStatus, 1)
        );

        if (app != null) {
            // 缓存应用信息 - 转换Service层Apps为API层AppsDTO
            cacheService.cacheAppInfo(AppsConverter.toDTO(app));
            log.debug("从数据库获取应用信息并缓存，appId: {}", appId);
        }

        return app;
    }

}