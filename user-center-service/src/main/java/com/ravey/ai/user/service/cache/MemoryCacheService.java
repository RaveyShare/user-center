package com.ravey.ai.user.service.cache;

import com.ravey.ai.user.api.constants.CacheConstants;
import com.ravey.ai.user.api.dto.AppsDTO;
import com.ravey.ai.user.api.dto.UsersDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 内存缓存服务（用于测试环境）
 * 当 Redis 不可用时使用内存缓存
 *
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
@Service
@ConditionalOnMissingBean(RedisTemplate.class)
public class MemoryCacheService {

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    /**
     * 缓存微信小程序 Access Token
     */
    public void cacheMiniAppAccessToken(String appId, String accessToken) {
        String key = CacheConstants.formatKey(CacheConstants.MINI_APP_ACCESS_TOKEN_KEY, appId);
        cache.put(key, accessToken);
        log.info("缓存微信小程序AccessToken: appId={}", appId);
    }

    /**
     * 获取微信小程序 Access Token
     */
    public String getMiniAppAccessToken(String appId) {
        String key = CacheConstants.formatKey(CacheConstants.MINI_APP_ACCESS_TOKEN_KEY, appId);
        Object token = cache.get(key);
        log.debug("获取微信小程序AccessToken: appId={}, found={}", appId, token != null);
        return token instanceof String ? (String) token : null;
    }

    /**
     * 缓存应用信息
     */
    public void cacheAppInfo(AppsDTO app) {
        String key = CacheConstants.formatKey(CacheConstants.APP_INFO_KEY, app.getAppId());
        cache.put(key, app);
        log.info("缓存应用信息: appId={}", app.getAppId());
    }

    /**
     * 获取应用信息
     */
    public AppsDTO getAppInfo(String appId) {
        String key = CacheConstants.formatKey(CacheConstants.APP_INFO_KEY, appId);
        Object app = cache.get(key);
        log.debug("获取应用信息: appId={}, found={}", appId, app != null);
        return app instanceof AppsDTO ? (AppsDTO) app : null;
    }

    /**
     * 缓存用户信息
     */
    public void cacheUserInfo(UsersDTO user) {
        String key = CacheConstants.formatKey(CacheConstants.USER_INFO_KEY, user.getId());
        cache.put(key, user);
        log.info("缓存用户信息: userId={}", user.getId());
    }

    /**
     * 获取用户信息
     */
    public UsersDTO getUserInfo(Long userId) {
        String key = CacheConstants.formatKey(CacheConstants.USER_INFO_KEY, userId);
        Object user = cache.get(key);
        log.debug("获取用户信息: userId={}, found={}", userId, user != null);
        return user instanceof UsersDTO ? (UsersDTO) user : null;
    }

    /**
     * 缓存用户会话
     */
    public void cacheUserSession(Long userId, String appId, String token) {
        String userTokenKey = CacheConstants.formatKey(CacheConstants.USER_TOKEN_KEY, userId, appId != null ? appId : "default");
        String tokenUserKey = CacheConstants.formatKey(CacheConstants.TOKEN_USER_KEY, token);
        
        cache.put(userTokenKey, token);
        cache.put(tokenUserKey, userId + ":" + (appId != null ? appId : "default"));
        
        log.info("缓存用户会话: userId={}, appId={}, token={}", userId, appId, token);
    }

    /**
     * 获取用户会话token
     */
    public String getUserToken(Long userId, String appId) {
        String key = CacheConstants.formatKey(CacheConstants.USER_TOKEN_KEY, userId, appId != null ? appId : "default");
        Object token = cache.get(key);
        log.debug("获取用户token: userId={}, appId={}, found={}", userId, appId, token != null);
        return token instanceof String ? (String) token : null;
    }

    /**
     * 根据token获取用户信息
     */
    public String getUserSession(String token) {
        String key = CacheConstants.formatKey(CacheConstants.TOKEN_USER_KEY, token);
        Object userInfo = cache.get(key);
        log.debug("根据token获取用户信息: token={}, found={}", token, userInfo != null);
        return userInfo instanceof String ? (String) userInfo : null;
    }

    /**
     * 移除用户会话
     */
    public void removeUserSession(String token) {
        String tokenUserKey = CacheConstants.formatKey(CacheConstants.TOKEN_USER_KEY, token);
        String userInfo = getUserSession(token);
        
        if (userInfo != null) {
            String[] parts = userInfo.split(":");
            if (parts.length == 2) {
                String userTokenKey = CacheConstants.formatKey(CacheConstants.USER_TOKEN_KEY, parts[0], parts[1]);
                cache.remove(userTokenKey);
            }
        }
        cache.remove(tokenUserKey);
        log.info("移除用户会话: token={}", token);
    }

    /**
     * 根据用户ID移除会话
     */
    public void removeUserSessionByUserId(Long userId, String appId) {
        String userTokenKey = CacheConstants.formatKey(CacheConstants.USER_TOKEN_KEY, userId, appId != null ? appId : "default");
        String token = getUserToken(userId, appId);
        
        if (token != null) {
            String tokenUserKey = CacheConstants.formatKey(CacheConstants.TOKEN_USER_KEY, token);
            cache.remove(tokenUserKey);
        }
        cache.remove(userTokenKey);
        log.info("根据用户ID移除会话: userId={}, appId={}", userId, appId);
    }

    /**
     * 缓存用户应用关联信息
     */
    public void cacheUserApp(Long userId, String appId, boolean exists) {
        String key = CacheConstants.formatKey(CacheConstants.USER_APP_KEY, userId, appId);
        cache.put(key, exists);
        log.debug("缓存用户应用关联: userId={}, appId={}, exists={}", userId, appId, exists);
    }

    /**
     * 获取用户应用关联信息
     */
    public Boolean getUserApp(Long userId, String appId) {
        String key = CacheConstants.formatKey(CacheConstants.USER_APP_KEY, userId, appId);
        Object exists = cache.get(key);
        log.debug("获取用户应用关联: userId={}, appId={}, found={}", userId, appId, exists != null);
        return exists instanceof Boolean ? (Boolean) exists : null;
    }

    /**
     * 缓存微信会话信息
     */
    public void cacheWeChatSession(String appId, String code, boolean used) {
        String key = CacheConstants.formatKey(CacheConstants.WECHAT_CODE_KEY, appId, code);
        cache.put(key, used);
        log.debug("缓存微信会话信息: appId={}, code={}, used={}", appId, code, used);
    }

    /**
     * 检查微信授权码是否已使用
     */
    public boolean isWeChatCodeUsed(String appId, String code) {
        String key = CacheConstants.formatKey(CacheConstants.WECHAT_CODE_KEY, appId, code);
        Object used = cache.get(key);
        boolean isUsed = used instanceof Boolean ? (Boolean) used : false;
        log.debug("检查微信授权码: appId={}, code={}, used={}", appId, code, isUsed);
        return isUsed;
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        cache.remove(key);
        log.debug("删除缓存: key={}", key);
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(String key) {
        boolean exists = cache.containsKey(key);
        log.debug("检查缓存存在: key={}, exists={}", key, exists);
        return exists;
    }

    /**
     * 设置缓存过期时间（内存缓存暂不支持过期时间）
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        log.debug("内存缓存不支持过期时间设置: key={}", key);
    }

    /**
     * 刷新用户会话过期时间（内存缓存暂不支持）
     */
    public void refreshUserSession(String token) {
        log.debug("内存缓存不支持刷新会话过期时间: token={}", token);
    }

    /**
     * 设置过期时间（内存缓存暂不支持）
     */
    public void setExpiration(String key, long timeout, TimeUnit unit) {
        log.debug("内存缓存不支持设置过期时间: key={}", key);
    }
}