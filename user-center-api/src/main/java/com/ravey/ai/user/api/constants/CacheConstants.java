package com.ravey.ai.user.api.constants;

/**
 * 缓存常量类
 *
 * @author ravey
 * @since 1.0.0
 */
public class CacheConstants {

    /**
     * 微信小程序 Access Token 缓存键
     * 格式：mini_app:access_token:{appId}
     */
    public static final String MINI_APP_ACCESS_TOKEN_KEY = "mini_app:access_token:{}";

    /**
     * 应用信息缓存键
     * 格式：app:info:{appId}
     */
    public static final String APP_INFO_KEY = "app:info:{}";

    /**
     * 用户信息缓存键
     * 格式：user:info:{userId}
     */
    public static final String USER_INFO_KEY = "user:info:{}";

    /**
     * 用户会话缓存键
     * 格式：user:session:{token}
     */
    public static final String USER_SESSION_KEY = "user:session:{}";

    /**
     * 用户Token缓存键（参考 new-retail-guide 双向缓存设计）
     * 格式：user:token:{userId}:{appId}
     */
    public static final String USER_TOKEN_KEY = "user:token:{}:{}";

    /**
     * Token到用户的反向缓存键
     * 格式：token:user:{token}
     */
    public static final String TOKEN_USER_KEY = "token:user:{}";

    /**
     * 用户应用关联缓存键
     * 格式：user:app:{userId}:{appId}
     */
    public static final String USER_APP_KEY = "user:app:{}:{}";

    /**
     * 微信会话信息缓存键
     * 格式：wechat:session:{appId}:{code}
     */
    public static final String WECHAT_SESSION_KEY = "wechat:session:{}:{}";

    /**
     * 微信授权码缓存键
     * 格式：wechat:code:{appId}:{code}
     */
    public static final String WECHAT_CODE_KEY = "wechat:code:{}:{}";

    /**
     * 二维码登录确认后的临时Token缓存键
     * 格式：qr:token:{qrcodeId}
     */
    public static final String QR_TOKEN_KEY = "qr:token:{}";

    // 缓存过期时间常量（秒）
    
    /**
     * 微信 Access Token 缓存时间：110分钟（微信Token有效期2小时，提前10分钟刷新）
     */
    public static final long MINI_APP_ACCESS_TOKEN_EXPIRE = 110 * 60;

    /**
     * 应用信息缓存时间：7天
     */
    public static final long APP_INFO_EXPIRE = 7 * 24 * 60 * 60;

    /**
     * 用户信息缓存时间：1小时
     */
    public static final long USER_INFO_EXPIRE = 60 * 60;

    /**
     * 用户会话缓存时间：2小时（与JWT过期时间一致）
     */
    public static final long USER_SESSION_EXPIRE = 2 * 60 * 60;

    /**
     * 用户应用关联缓存时间：30分钟
     */
    public static final long USER_APP_EXPIRE = 30 * 60;

    /**
     * 微信会话信息缓存时间：5分钟（微信code只能使用一次，短时间缓存防重复）
     */
    public static final long WECHAT_SESSION_EXPIRE = 5 * 60;

    /**
     * 二维码登录临时Token缓存时间：5分钟
     */
    public static final long QR_TOKEN_EXPIRE = 5 * 60;

    /**
     * 格式化缓存键
     *
     * @param template 缓存键模板
     * @param params   参数
     * @return 格式化后的缓存键
     */
    public static String formatKey(String template, Object... params) {
        String result = template;
        for (Object param : params) {
            result = result.replaceFirst("\\{\\}", String.valueOf(param));
        }
        return result;
    }
}