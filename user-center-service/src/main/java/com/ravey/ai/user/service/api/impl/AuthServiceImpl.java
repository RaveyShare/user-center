package com.ravey.ai.user.service.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ravey.ai.user.api.dto.AppsDTO;
import com.ravey.ai.user.api.dto.UsersDTO;
import com.ravey.ai.user.api.model.dto.WeChatSessionDto;
import com.ravey.ai.user.api.model.req.MiniProgramLoginReq;
import com.ravey.ai.user.api.model.res.MiniProgramLoginRes;
import com.ravey.ai.user.api.model.req.QrGenerateReq;
import com.ravey.ai.user.api.model.req.QrCheckReq;
import com.ravey.ai.user.api.model.req.QrScanReq;
import com.ravey.ai.user.api.model.req.QrConfirmReq;
import com.ravey.ai.user.api.model.res.QrGenerateRes;
import com.ravey.ai.user.api.model.res.QrCheckRes;
import com.ravey.ai.user.api.model.req.WxaCodeReq;
import com.ravey.ai.user.api.model.res.WxaCodeRes;
import com.ravey.ai.user.api.service.AuthService;
import com.ravey.ai.user.service.cache.CacheService;
import com.ravey.ai.user.api.utils.JwtUtils;
import com.ravey.ai.user.service.converter.UsersConverter;
import com.ravey.ai.user.service.dao.entity.Apps;
import com.ravey.ai.user.service.dao.entity.UserApps;
import com.ravey.ai.user.service.dao.entity.UserSessions;
import com.ravey.ai.user.service.dao.entity.Users;
import com.ravey.ai.user.service.dao.entity.QrLoginRecords;
import com.ravey.ai.user.service.dao.mapper.AppsMapper;
import com.ravey.ai.user.service.dao.mapper.UserAppsMapper;
import com.ravey.ai.user.service.dao.mapper.UserSessionsMapper;
import com.ravey.ai.user.service.dao.mapper.UsersMapper;
import com.ravey.ai.user.service.dao.mapper.QrLoginRecordsMapper;
import com.ravey.ai.user.service.context.UserContext;
import com.ravey.ai.user.api.enums.UserErrorCode;
import com.ravey.common.api.model.ServiceException;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

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
    @Resource
    private QrLoginRecordsMapper qrLoginRecordsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniProgramLoginRes miniProgramLogin(MiniProgramLoginReq req) {
        log.info("小程序登录开始: appId={}", req.getAppId());
        
        try {
            // 1. 获取微信会话信息
            WeChatSessionDto weChatSession = getWeChatSession(req.getAppId(), req.getCode());

            // 尝试通过解密数据获取UnionID
            if (!StringUtils.hasText(weChatSession.getUnionid()) && 
                StringUtils.hasText(req.getEncryptedData()) && 
                StringUtils.hasText(req.getIv())) {
                try {
                    WxMaUserInfo wxMaUserInfo = weChatService.decryptUserInfo(
                        req.getAppId(), 
                        weChatSession.getSessionKey(), 
                        req.getEncryptedData(), 
                        req.getIv()
                    );
                    if (wxMaUserInfo != null) {
                        if (StringUtils.hasText(wxMaUserInfo.getUnionId())) {
                            weChatSession.setUnionid(wxMaUserInfo.getUnionId());
                            log.info("通过解密获取到UnionID: {}", wxMaUserInfo.getUnionId());
                        }
                        
                        // 如果前端没有传递明文用户信息，尝试从解密数据中获取
                        if (req.getUserInfo() == null) {
                            req.setUserInfo(new MiniProgramLoginReq.UserInfo());
                        }
                        if (!StringUtils.hasText(req.getUserInfo().getNickname()) && StringUtils.hasText(wxMaUserInfo.getNickName())) {
                            req.getUserInfo().setNickname(wxMaUserInfo.getNickName());
                        }
                        if (!StringUtils.hasText(req.getUserInfo().getAvatarUrl()) && StringUtils.hasText(wxMaUserInfo.getAvatarUrl())) {
                            req.getUserInfo().setAvatarUrl(wxMaUserInfo.getAvatarUrl());
                        }
                    }
                } catch (Exception e) {
                    log.warn("解密用户信息失败", e);
                }
            }
            
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
            throw new ServiceException(UserErrorCode.WECHAT_LOGIN_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QrGenerateRes generateQr(QrGenerateReq req) {
        Apps app = validateAndGetApp(req.getAppId());
        QrLoginRecords record = new QrLoginRecords();
        String qrcodeId = UUID.randomUUID().toString().replace("-", "");
        record.setId(IdWorker.getId());
        record.setQrcodeId(qrcodeId);
        record.setAppId(app.getId());
        record.setStatus(0);
        LocalDateTime expire = LocalDateTime.now().plusMinutes(5);
        record.setExpireTime(expire);
        qrLoginRecordsMapper.insert(record);

        QrGenerateRes res = new QrGenerateRes();
        res.setQrcodeId(qrcodeId);
        res.setExpireAt(expire.toInstant(ZoneOffset.UTC).toEpochMilli());
        res.setQrContent("ravey-login://qr?qrcodeId=" + qrcodeId);
        return res;
    }

    @Override
    public QrCheckRes checkQr(QrCheckReq req) {
        QrLoginRecords record = qrLoginRecordsMapper.selectOne(
                new LambdaQueryWrapper<QrLoginRecords>()
                        .eq(QrLoginRecords::getQrcodeId, req.getQrcodeId())
                        .last("LIMIT 1")
        );

        QrCheckRes res = new QrCheckRes();
        if (record == null) {
            res.setStatus(3);
            return res;
        }

        if (record.getExpireTime() != null && record.getExpireTime().isBefore(LocalDateTime.now())) {
            record.setStatus(3);
            qrLoginRecordsMapper.updateById(record);
        }

        res.setStatus(record.getStatus());
        if (record.getStatus() != null && record.getStatus() == 2) {
            String token = cacheService.getQrToken(record.getQrcodeId());
            res.setToken(token);
            if (record.getUserId() != null) {
                Users user = usersMapper.selectById(record.getUserId());
                if (user != null) {
                    MiniProgramLoginRes.UserInfo info = new MiniProgramLoginRes.UserInfo();
                    info.setId(user.getId());
                    info.setNickname(user.getNickname());
                    info.setAvatarUrl(user.getAvatarUrl());
                    res.setUserInfo(info);
                }
            }
        }
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scanQr(QrScanReq req) {
        QrLoginRecords record = qrLoginRecordsMapper.selectOne(
                new LambdaQueryWrapper<QrLoginRecords>()
                        .eq(QrLoginRecords::getQrcodeId, req.getQrcodeId())
                        .last("LIMIT 1")
        );
        if (record == null) {
            throw new ServiceException(UserErrorCode.QR_CODE_NOT_FOUND);
        }
        if (record.getExpireTime() != null && record.getExpireTime().isBefore(LocalDateTime.now())) {
            record.setStatus(3);
            qrLoginRecordsMapper.updateById(record);
            throw new ServiceException(UserErrorCode.QR_CODE_EXPIRED);
        }
        Long userId = UserContext.getCurrentUser() != null ? UserContext.getCurrentUser().getId() : null;
        if (userId == null) {
            throw new ServiceException(UserErrorCode.NOT_LOGGED_IN);
        }
        record.setUserId(userId);
        record.setStatus(1);
        qrLoginRecordsMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmQr(QrConfirmReq req) {
        QrLoginRecords record = qrLoginRecordsMapper.selectOne(
                new LambdaQueryWrapper<QrLoginRecords>()
                        .eq(QrLoginRecords::getQrcodeId, req.getQrcodeId())
                        .last("LIMIT 1")
        );
        if (record == null) {
            throw new ServiceException(UserErrorCode.QR_CODE_NOT_FOUND);
        }
        if (record.getExpireTime() != null && record.getExpireTime().isBefore(LocalDateTime.now())) {
            record.setStatus(3);
            qrLoginRecordsMapper.updateById(record);
            throw new ServiceException(UserErrorCode.QR_CODE_EXPIRED);
        }
        Long userId = UserContext.getCurrentUser() != null ? UserContext.getCurrentUser().getId() : null;
        if (userId == null) {
            throw new ServiceException(UserErrorCode.NOT_LOGGED_IN);
        }
        record.setUserId(userId);
        record.setStatus(2);
        qrLoginRecordsMapper.updateById(record);

        Apps targetApp = appsMapper.selectById(record.getAppId());
        if (targetApp == null) {
            throw new ServiceException(UserErrorCode.APP_DISABLED);
        }
        String accessToken = generateAccessToken(userId, targetApp.getAppId());
        createAndCacheUserSession(userId, targetApp.getId(), accessToken);
        cacheService.cacheQrToken(record.getQrcodeId(), accessToken);
    }

    @Override
    public WxaCodeRes generateWxaCode(WxaCodeReq req) {
        Apps app = validateAndGetApp(req.getAppId());

        QrLoginRecords record = qrLoginRecordsMapper.selectOne(
                new LambdaQueryWrapper<QrLoginRecords>()
                        .eq(QrLoginRecords::getQrcodeId, req.getQrcodeId())
                        .last("LIMIT 1")
        );
        if (record == null) {
            throw new ServiceException(UserErrorCode.QR_CODE_NOT_FOUND);
        }
        if (record.getExpireTime() != null && record.getExpireTime().isBefore(LocalDateTime.now())) {
            record.setStatus(3);
            qrLoginRecordsMapper.updateById(record);
            throw new ServiceException(UserErrorCode.QR_CODE_EXPIRED);
        }

        byte[] cached = cacheService.getWxaCode(req.getQrcodeId());
        byte[] bytes = cached != null ? cached : weChatService.getWxaCodeUnlimited(
                app.getAppId(),
                req.getQrcodeId(),
                req.getPage(),
                req.getWidth(),
                req.getEnvVersion(),
                req.getCheckPath(),
                req.getHyaline()
        );
        if (bytes == null || bytes.length == 0) {
            throw new ServiceException(UserErrorCode.SYSTEM_ERROR);
        }
        if (cached == null) {
            cacheService.cacheWxaCode(req.getQrcodeId(), bytes);
        }
        String base64 = java.util.Base64.getEncoder().encodeToString(bytes);

        WxaCodeRes res = new WxaCodeRes();
        res.setQrcodeId(record.getQrcodeId());
        res.setExpireAt(record.getExpireTime() != null ? record.getExpireTime().toInstant(ZoneOffset.UTC).toEpochMilli() : null);
        res.setImageBase64(base64);
        return res;
    }

    /**
     * 获取微信会话信息
     */
    private WeChatSessionDto getWeChatSession(String appId, String code) {
        WeChatSessionDto weChatSession = weChatService.getWeChatSession(appId, code);
        
        if (weChatSession == null || (weChatSession.getErrcode() != null && weChatSession.getErrcode() != 0)) {
            String errorMsg = weChatSession != null ? weChatSession.getErrmsg() : "未知错误";
            throw new ServiceException(UserErrorCode.WECHAT_LOGIN_FAILED);
        }
        
        return weChatSession;
    }

    /**
     * 验证并获取应用信息
     */
    private Apps validateAndGetApp(String appId) {
        Apps app = getAppByAppId(appId);
        if (app == null) {
            throw new ServiceException(UserErrorCode.APP_NOT_FOUND);
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
