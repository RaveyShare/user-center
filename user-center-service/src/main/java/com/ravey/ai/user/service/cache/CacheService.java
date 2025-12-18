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
 * @author ravey
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
     * 设置缓存
     * 
     * @param key     键
     * @param value   值
     * @param timeout 过期时间（秒）
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存
     * 
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        Object val = redisTemplate.opsForValue().get(key);
        return val != null ? val.toString() : null;
    }

    /**
     * 缓存微信小程序 Access Token
     *
     * @param appId       应用ID
     * @param accessToken Access Token
     */
    public void cacheMiniAppAccessToken(String appId, String accessToken) {
        String key = CacheConstants.formatKey(CacheConstants.MINI_APP_ACCESS_TOKEN_KEY, appId);
        redisTemplate.opsForValue().set(key, accessToken, CacheConstants.MINI_APP_ACCESS_TOKEN_EXPIRE,
                TimeUnit.SECONDS);
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
     * 缓存用户会话信息（参考 new-retail-guide 的双向缓存策略）
     *
     * @param token  JWT Token
     * @param userId 用户ID
     */
    public void cacheUserSession(String token, Long userId) {
        // 双向缓存：token -> userId 和 userId -> token
        String tokenKey = CacheConstants.formatKey(CacheConstants.USER_SESSION_KEY, token);
        String userKey = CacheConstants.formatKey(CacheConstants.USER_TOKEN_KEY, userId);

        // 缓存 token -> userId
        redisTemplate.opsForValue().set(tokenKey, userId, CacheConstants.USER_SESSION_EXPIRE, TimeUnit.SECONDS);

        // 缓存 userId -> token（便于后续查找和管理）
        redisTemplate.opsForValue().set(userKey, token, CacheConstants.USER_SESSION_EXPIRE, TimeUnit.SECONDS);

        log.info("缓存用户会话（双向）: userId={}, token={}", userId, token.substring(0, 10) + "...");
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
     * 根据用户ID获取Token（参考 new-retail-guide 设计）
     *
     * @param userId 用户ID
     * @return JWT Token
     */
    public String getUserToken(Long userId) {
        String key = CacheConstants.formatKey(CacheConstants.USER_TOKEN_KEY, userId);
        Object token = redisTemplate.opsForValue().get(key);
        log.debug("根据用户ID获取Token: userId={}, found={}", userId, token != null);
        return token != null ? token.toString() : null;
    }

    /**
     * 删除用户会话（双向删除）
     *
     * @param token JWT Token
     */
    public void removeUserSession(String token) {
        // 先获取用户ID
        Long userId = getUserSession(token);

        // 删除 token -> userId 缓存
        String tokenKey = CacheConstants.formatKey(CacheConstants.USER_SESSION_KEY, token);
        redisTemplate.delete(tokenKey);

        // 删除 userId -> token 缓存
        if (userId != null) {
            String userKey = CacheConstants.formatKey(CacheConstants.USER_TOKEN_KEY, userId);
            redisTemplate.delete(userKey);
        }

        log.info("删除用户会话（双向）: userId={}", userId);
    }

    /**
     * 根据用户ID删除会话（参考 new-retail-guide 设计）
     *
     * @param userId 用户ID
     */
    public void removeUserSessionByUserId(Long userId) {
        // 先获取Token
        String token = getUserToken(userId);

        // 删除 userId -> token 缓存
        String userKey = CacheConstants.formatKey(CacheConstants.USER_TOKEN_KEY, userId);
        redisTemplate.delete(userKey);

        // 删除 token -> userId 缓存
        if (token != null) {
            String tokenKey = CacheConstants.formatKey(CacheConstants.USER_SESSION_KEY, token);
            redisTemplate.delete(tokenKey);
        }

        log.info("根据用户ID删除会话（双向）: userId={}, token={}", userId,
                token != null ? token.substring(0, 10) + "..." : "null");
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

    /**
     * 刷新用户会话过期时间（参考 new-retail-guide 设计）
     *
     * @param token  JWT Token
     * @param userId 用户ID
     */
    public void refreshUserSession(String token, Long userId) {
        String tokenKey = CacheConstants.formatKey(CacheConstants.USER_SESSION_KEY, token);
        String userKey = CacheConstants.formatKey(CacheConstants.USER_TOKEN_KEY, userId);

        // 刷新两个缓存的过期时间
        redisTemplate.expire(tokenKey, CacheConstants.USER_SESSION_EXPIRE, TimeUnit.SECONDS);
        redisTemplate.expire(userKey, CacheConstants.USER_SESSION_EXPIRE, TimeUnit.SECONDS);

        log.debug("刷新用户会话过期时间: userId={}, token={}", userId, token.substring(0, 10) + "...");
    }

    public void cacheQrToken(String qrcodeId, String token) {
        String key = CacheConstants.formatKey(CacheConstants.QR_TOKEN_KEY, qrcodeId);
        redisTemplate.opsForValue().set(key, token, CacheConstants.QR_TOKEN_EXPIRE, TimeUnit.SECONDS);
        log.debug("缓存二维码登录Token: qrcodeId={}", qrcodeId);
    }

    public String getQrToken(String qrcodeId) {
        String key = CacheConstants.formatKey(CacheConstants.QR_TOKEN_KEY, qrcodeId);
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }

    /**
     * 缓存小程序码图片字节
     *
     * @param qrcodeId 二维码ID
     * @param bytes    图片字节
     */
    public void cacheWxaCode(String qrcodeId, byte[] bytes) {
        String key = CacheConstants.formatKey(CacheConstants.WXA_CODE_KEY, qrcodeId);
        redisTemplate.opsForValue().set(key, bytes, CacheConstants.WXA_CODE_EXPIRE, TimeUnit.SECONDS);
        log.debug("缓存小程序码: qrcodeId={}", qrcodeId);
    }

    /**
     * 获取已缓存的小程序码图片字节
     *
     * @param qrcodeId 二维码ID
     * @return 图片字节
     */
    public byte[] getWxaCode(String qrcodeId) {
        String key = CacheConstants.formatKey(CacheConstants.WXA_CODE_KEY, qrcodeId);
        Object val = redisTemplate.opsForValue().get(key);
        return val instanceof byte[] ? (byte[]) val : null;
    }
}