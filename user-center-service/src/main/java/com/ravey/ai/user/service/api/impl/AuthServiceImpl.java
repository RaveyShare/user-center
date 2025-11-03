package com.ravey.ai.user.service.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ravey.ai.user.api.dto.AppsDTO;
import com.ravey.ai.user.api.dto.UsersDTO;
import com.ravey.ai.user.api.model.dto.WeChatSessionDto;
import com.ravey.ai.user.api.model.req.MiniProgramLoginReq;
import com.ravey.ai.user.api.model.res.MiniProgramLoginRes;
import com.ravey.ai.user.api.service.AuthService;
import com.ravey.ai.user.service.cache.CacheService;
import com.ravey.ai.user.api.utils.JwtUtils;
import com.ravey.ai.user.service.converter.UsersConverter;
import com.ravey.ai.user.service.dao.entity.Apps;
import com.ravey.ai.user.service.dao.entity.UserApps;
import com.ravey.ai.user.service.dao.entity.UserSessions;
import com.ravey.ai.user.service.dao.entity.Users;
import com.ravey.ai.user.service.dao.mapper.AppsMapper;
import com.ravey.ai.user.service.dao.mapper.UserAppsMapper;
import com.ravey.ai.user.service.dao.mapper.UserSessionsMapper;
import com.ravey.ai.user.service.dao.mapper.UsersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
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
public class AuthServiceImpl implements AuthService {

    @Resource
    private WeChatServiceImpl weChatService;
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private CacheService cacheService;
    @Resource
    private AppsMapper appsMapper;
    @Resource
    private UsersMapper usersMapper;
    @Resource
    private UserAppsMapper userAppsMapper;
    @Resource
    private UserSessionsMapper userSessionsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniProgramLoginRes miniProgramLogin(MiniProgramLoginReq req) {
        log.info("小程序登录开始: appId={}", req.getAppId());
        
        try {
            // 1. 获取微信会话信息
            WeChatSessionDto weChatSession = getWeChatSession(req.getAppId(), req.getCode());
            
            // 2. 验证应用信息
            Apps app = validateAndGetApp(req.getAppId());
            
            // 3. 查找或创建用户
            Users user = findOrCreateUser(app.getId(), weChatSession, req.getUserInfo());
            
            // 4. 生成访问令牌
            String accessToken = generateAccessToken(user.getId(), app.getAppId());
            
            // 5. 创建用户会话
            createAndCacheUserSession(user.getId(), app.getId(), accessToken);
            
            // 6. 构建响应结果
            MiniProgramLoginRes response = buildLoginResponse(user, accessToken);
            
            log.info("小程序登录成功: appId={}, userId={}", req.getAppId(), user.getId());
            return response;
            
        } catch (Exception e) {
            log.error("小程序登录失败: appId={}, error={}", req.getAppId(), e.getMessage(), e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取微信会话信息
     */
    private WeChatSessionDto getWeChatSession(String appId, String code) {
        WeChatSessionDto weChatSession = weChatService.getWeChatSession(appId, code);
        
        if (weChatSession == null || (weChatSession.getErrcode() != null && weChatSession.getErrcode() != 0)) {
            String errorMsg = weChatSession != null ? weChatSession.getErrmsg() : "未知错误";
            throw new RuntimeException("微信登录失败: " + errorMsg);
        }
        
        return weChatSession;
    }

    /**
     * 验证并获取应用信息
     */
    private Apps validateAndGetApp(String appId) {
        Apps app = getAppByAppId(appId);
        if (app == null) {
            throw new RuntimeException("应用不存在或已禁用: " + appId);
        }
        return app;
    }

    /**
     * 生成访问令牌
     */
    private String generateAccessToken(Long userId, String appId) {
        return jwtUtils.generateToken(userId, appId);
    }

    /**
     * 创建并缓存用户会话
     */
    private void createAndCacheUserSession(Long userId, Long appId, String accessToken) {
        // 创建会话记录
        createUserSession(userId, appId, accessToken);
        
        // 缓存会话信息
        cacheUserSession(accessToken, userId);
        
        // 缓存用户信息
        Users user = usersMapper.selectById(userId);
        if (user != null) {
            cacheUserInfo(UsersConverter.toDTO(user));
        }
    }

    /**
     * 构建登录响应
     */
    private MiniProgramLoginRes buildLoginResponse(Users user, String accessToken) {
        MiniProgramLoginRes response = new MiniProgramLoginRes();
        response.setToken(accessToken);

        MiniProgramLoginRes.UserInfo userInfo = new MiniProgramLoginRes.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        response.setUserInfo(userInfo);

        return response;
    }

    /**
     * 查找或创建用户
     */
    private Users findOrCreateUser(Long appId, WeChatSessionDto weChatSession, MiniProgramLoginReq.UserInfo userInfo) {
        // 1. 优先通过 openid 查找现有用户应用关联
        UserApps existingUserApp = findUserAppByOpenid(appId, weChatSession.getOpenid());
        
        if (existingUserApp != null) {
            // 找到现有关联，更新 unionid 并返回用户
            Users user = usersMapper.selectById(existingUserApp.getUserId());
            updateUserAppUnionId(existingUserApp, weChatSession.getUnionid());
            updateUserInfo(user, userInfo);
            return user;
        }
        
        // 2. 通过 unionid 查找现有用户（如果有 unionid）
        Users user = findUserByUnionid(weChatSession.getUnionid());
        
        if (user == null) {
            // 3. 创建新用户
            user = createNewUser(userInfo);
        }
        
        // 4. 创建用户应用关联
        createUserAppAssociation(user.getId(), appId, weChatSession);
        
        // 5. 更新用户信息
        updateUserInfo(user, userInfo);
        
        return user;
    }

    /**
     * 通过 openid 查找用户应用关联
     */
    private UserApps findUserAppByOpenid(Long appId, String openid) {
        return userAppsMapper.selectOne(
                new LambdaQueryWrapper<UserApps>()
                        .eq(UserApps::getAppId, appId)
                        .eq(UserApps::getOpenid, openid)
                        .eq(UserApps::getStatus, 1)
        );
    }

    /**
     * 通过 unionid 查找用户
     */
    private Users findUserByUnionid(String unionid) {
        if (!StringUtils.hasText(unionid)) {
            return null;
        }
        
        UserApps unionidUserApp = userAppsMapper.selectOne(
                new LambdaQueryWrapper<UserApps>()
                        .eq(UserApps::getUnionid, unionid)
                        .eq(UserApps::getStatus, 1)
                        .last("LIMIT 1")
        );
        
        return unionidUserApp != null ? usersMapper.selectById(unionidUserApp.getUserId()) : null;
    }

    /**
     * 创建新用户
     */
    private Users createNewUser(MiniProgramLoginReq.UserInfo userInfo) {
        Users user = new Users();
        if (userInfo != null) {
            user.setNickname(userInfo.getNickname());
            user.setAvatarUrl(userInfo.getAvatarUrl());
        }
        user.setStatus(1);
        usersMapper.insert(user);
        
        log.info("创建新用户成功: userId={}", user.getId());
        return user;
    }

    /**
     * 创建用户应用关联
     */
    private void createUserAppAssociation(Long userId, Long appId, WeChatSessionDto weChatSession) {
        UserApps userApp = new UserApps();
        userApp.setUserId(userId);
        userApp.setAppId(appId);
        userApp.setOpenid(weChatSession.getOpenid());
        userApp.setUnionid(weChatSession.getUnionid());
        userApp.setStatus(1);
        userAppsMapper.insert(userApp);
        
        log.info("创建用户应用关联成功: userId={}, appId={}", userId, appId);
    }

    /**
     * 更新用户应用关联的 unionid
     */
    private void updateUserAppUnionId(UserApps userApp, String newUnionid) {
        if (StringUtils.hasText(newUnionid) && !newUnionid.equals(userApp.getUnionid())) {
            userApp.setUnionid(newUnionid);
            userAppsMapper.updateById(userApp);
            log.debug("更新用户应用关联 unionid: userAppId={}", userApp.getId());
        }
    }

    /**
     * 更新用户信息
     */
    private void updateUserInfo(Users user, MiniProgramLoginReq.UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        
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
            log.debug("更新用户信息: userId={}", user.getId());
        }
    }

    /**
     * 创建用户会话记录
     */
    private UserSessions createUserSession(Long userId, Long appId, String token) {
        UserSessions session = new UserSessions();
        session.setUserId(userId);
        session.setAppId(appId);
        session.setSessionToken(token);
        session.setExpireTime(LocalDateTime.now().plusDays(1));
        userSessionsMapper.insert(session);
        
        log.debug("创建用户会话记录: userId={}, sessionId={}", userId, session.getId());
        return session;
    }

    /**
     * 获取应用信息（优先从缓存获取）
     */
    private Apps getAppByAppId(String appId) {
        // 先从缓存获取
        AppsDTO appDto = getAppInfoFromCache(appId);
        if (appDto != null) {
            return convertDtoToEntity(appDto);
        }
        
        // 从数据库查询
        Apps app = appsMapper.selectOne(
                new LambdaQueryWrapper<Apps>()
                        .eq(Apps::getAppId, appId)
                        .eq(Apps::getStatus, 1)
        );
        
        // 缓存应用信息
        if (app != null) {
            AppsDTO dto = convertEntityToDto(app);
            cacheAppInfo(dto);
        }
        
        return app;
    }

    /**
     * 转换 DTO 为实体
     */
    private Apps convertDtoToEntity(AppsDTO dto) {
        Apps app = new Apps();
        app.setId(dto.getId());
        app.setAppId(dto.getAppId());
        app.setAppName(dto.getAppName());
        app.setAppSecret(dto.getAppSecret());
        app.setStatus(dto.getStatus());
        return app;
    }

    /**
     * 转换实体为 DTO
     */
    private AppsDTO convertEntityToDto(Apps app) {
        AppsDTO dto = new AppsDTO();
        dto.setId(app.getId());
        dto.setAppId(app.getAppId());
        dto.setAppName(app.getAppName());
        dto.setAppSecret(app.getAppSecret());
        dto.setStatus(app.getStatus());
        return dto;
    }

    // ==================== 缓存相关方法 ====================

    /**
     * 缓存用户会话
     */
    private void cacheUserSession(String token, Long userId) {
        if (cacheService != null) {
            cacheService.cacheUserSession(token, userId);
        }
    }

    /**
     * 缓存用户信息
     */
    private void cacheUserInfo(UsersDTO userDto) {
        if (cacheService != null) {
            cacheService.cacheUserInfo(userDto);
        }
    }

    /**
     * 从缓存获取应用信息
     */
    private AppsDTO getAppInfoFromCache(String appId) {
        if (cacheService != null) {
            return cacheService.getAppInfo(appId);
        }
        return null;
    }

    /**
     * 缓存应用信息
     */
    private void cacheAppInfo(AppsDTO appDto) {
        if (cacheService != null) {
            cacheService.cacheAppInfo(appDto);
        }
    }
}