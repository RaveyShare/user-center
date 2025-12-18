package com.ravey.ai.user.service.component;

import com.ravey.ai.user.service.cache.CacheService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Random;

/**
 * 验证码服务
 *
 * @author Ravey
 */
@Component
public class VerificationCodeService {

    @Resource
    private CacheService cacheService;

    private static final String CODE_PREFIX = "verify_code:";
    private static final int EXPIRE_MINUTES = 5;

    /**
     * 生成验证码并保存到Redis
     *
     * @param email 邮箱
     * @param scene 场景
     * @return 验证码
     */
    public String generateCode(String email, Integer scene) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        String key = getCacheKey(email, scene);
        cacheService.set(key, code, (long) EXPIRE_MINUTES * 60);
        return code;
    }

    /**
     * 校验验证码
     *
     * @param email 邮箱
     * @param scene 场景
     * @param code  提交的验证码
     * @return 是否校验通过
     */
    public boolean verifyCode(String email, Integer scene, String code) {
        String key = getCacheKey(email, scene);
        String cachedCode = cacheService.get(key);
        if (code != null && code.equals(cachedCode)) {
            cacheService.delete(key);
            return true;
        }
        return false;
    }

    private String getCacheKey(String email, Integer scene) {
        return CODE_PREFIX + scene + ":" + email;
    }
}
