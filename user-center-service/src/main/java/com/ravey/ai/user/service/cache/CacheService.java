package com.ravey.ai.user.service.cache;

import com.ravey.ai.user.api.constants.CacheConstants;
import com.ravey.ai.user.api.dto.AppsDTO;
import com.ravey.ai.user.api.dto.UsersDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 统一缓存服务
 * 管理各种缓存操作
 *
 * @author Ravey
 * @since 1.0.0
 */
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
@Service
@RequiredArgsConstructor
    public class CacheService {

    @Qualifier("redisTemplate")
    private final RedisTemplate<Object, Object> redisTemplate;

    /**
     * 缓存微信小程序 Access Token
     *
     * @param appId       应用ID
     * @param accessToken Access Token
     */
    public void cacheMiniAppAccessToken(String appId, String accessToken) {
        String key = CacheConstants.formatKey(CacheConstants.MINI_APP_ACCESS_TOKEN_KEY, appId);
        redisTemplate.opsForValue().set(key, accessToken, CacheConstants.MINI_APP_ACCESS_TOKEN_EXPIRE, TimeUnit.SECONDS);
        log.info("缓存微信小程序AccessToken: appId={}", appId);
    }

    /**
     * 获取微信小程序 Access Token
     *
     * @param appId 应用ID
     * @return Access Token
     */
    public String getMiniAppAccessToken(String appId) {
        String key = CacheConstants.formatKey(CacheConstants.MINI_APP_ACCESS_TOKEN_KEY, appId);
        Object token = redisTemplate.opsForValue().get(key);
        log.debug("获取微信小程序AccessToken: appId={}, found={}", appId, token != null);
        return token != null ? token.toString() : null;
    }

    /**
     * 缓存应用信息
     *
     * @param app 应用信息
     */
    public void cacheAppInfo(AppsDTO app) {
        String key = CacheConstants.formatKey(CacheConstants.APP_INFO_KEY, app.getAppId());
        redisTemplate.opsForValue().set(key, app, CacheConstants.APP_INFO_EXPIRE, TimeUnit.SECONDS);
        log.info("缓存应用信息: appId={}", app.getAppId());
    }

    /**
     * 获取应用信息
     *
     * @param appId 应用ID
     * @return 应用信息
     */
    public AppsDTO getAppInfo(String appId) {
        String key = CacheConstants.formatKey(CacheConstants.APP_INFO_KEY, appId);
        Object app = redisTemplate.opsForValue().get(key);
        log.debug("获取应用信息: appId={}, found={}", appId, app != null);
        return app instanceof AppsDTO ? (AppsDTO) app : null;
    }

    /**
     * 缓存用户信息
     *
     * @param user 用户信息
     */
    public void cacheUserInfo(UsersDTO user) {
        String key = CacheConstants.formatKey(CacheConstants.USER_INFO_KEY, user.getId());
        redisTemplate.opsForValue().set(key, user, CacheConstants.USER_INFO_EXPIRE, TimeUnit.SECONDS);
        log.info("缓存用户信息: userId={}", user.getId());
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public UsersDTO getUserInfo(Long userId) {
        String key = CacheConstants.formatKey(CacheConstants.USER_INFO_KEY, userId);
        Object user = redisTemplate.opsForValue().get(key);
        log.debug("获取用户信息: userId={}, found={}", userId, user != null);
        return user instanceof UsersDTO ? (UsersDTO) user : null;
    }

    /**
     * 缓存用户会话信息
     *
     * @param token  JWT Token
     * @param userId 用户ID
     */
    public void cacheUserSession(String token, Long userId) {
        String key = CacheConstants.formatKey(CacheConstants.USER_SESSION_KEY, token);
        redisTemplate.opsForValue().set(key, userId, CacheConstants.USER_SESSION_EXPIRE, TimeUnit.SECONDS);
        log.info("缓存用户会话: userId={}", userId);
    }

    /**
     * 获取用户会话信息
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserSession(String token) {
        String key = CacheConstants.formatKey(CacheConstants.USER_SESSION_KEY, token);
        Object userId = redisTemplate.opsForValue().get(key);
        log.debug("获取用户会话: found={}", userId != null);
        return userId instanceof Number ? ((Number) userId).longValue() : null;
    }

    /**
     * 删除用户会话
     *
     * @param token JWT Token
     */
    public void removeUserSession(String token) {
        String key = CacheConstants.formatKey(CacheConstants.USER_SESSION_KEY, token);
        redisTemplate.delete(key);
        log.info("删除用户会话");
    }

    /**
     * 缓存用户应用关联信息
     *
     * @param userId 用户ID
     * @param appId  应用ID
     * @param exists 是否存在关联
     */
    public void cacheUserApp(Long userId, String appId, boolean exists) {
        String key = CacheConstants.formatKey(CacheConstants.USER_APP_KEY, userId, appId);
        redisTemplate.opsForValue().set(key, exists, CacheConstants.USER_APP_EXPIRE, TimeUnit.SECONDS);
        log.debug("缓存用户应用关联: userId={}, appId={}, exists={}", userId, appId, exists);
    }

    /**
     * 获取用户应用关联信息
     *
     * @param userId 用户ID
     * @param appId  应用ID
     * @return 是否存在关联，null表示缓存中不存在
     */
    public Boolean getUserApp(Long userId, String appId) {
        String key = CacheConstants.formatKey(CacheConstants.USER_APP_KEY, userId, appId);
        Object exists = redisTemplate.opsForValue().get(key);
        log.debug("获取用户应用关联: userId={}, appId={}, found={}", userId, appId, exists != null);
        return exists instanceof Boolean ? (Boolean) exists : null;
    }

    /**
     * 缓存微信会话信息（防止重复使用code）
     *
     * @param appId 应用ID
     * @param code  微信授权码
     * @param used  是否已使用
     */
    public void cacheWeChatSession(String appId, String code, boolean used) {
        String key = CacheConstants.formatKey(CacheConstants.WECHAT_SESSION_KEY, appId, code);
        redisTemplate.opsForValue().set(key, used, CacheConstants.WECHAT_SESSION_EXPIRE, TimeUnit.SECONDS);
        log.debug("缓存微信会话信息: appId={}, code={}, used={}", appId, code, used);
    }

    /**
     * 检查微信授权码是否已使用
     *
     * @param appId 应用ID
     * @param code  微信授权码
     * @return 是否已使用，null表示缓存中不存在
     */
    public Boolean isWeChatCodeUsed(String appId, String code) {
        String key = CacheConstants.formatKey(CacheConstants.WECHAT_SESSION_KEY, appId, code);
        Object used = redisTemplate.opsForValue().get(key);
        log.debug("检查微信授权码: appId={}, code={}, used={}", appId, code, used);
        return used instanceof Boolean ? (Boolean) used : null;
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("删除缓存: key={}", key);
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean exists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    /**
     * 设置缓存过期时间
     *
     * @param key     缓存键
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
        log.debug("设置缓存过期时间: key={}, timeout={}, unit={}", key, timeout, unit);
    }
}