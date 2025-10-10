package com.ravey.ai.user.service.context;

import com.ravey.ai.user.api.dto.UsersDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文工具类
 * 用于在请求处理过程中传递用户信息
 *
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
public class UserContext {

    private static final ThreadLocal<UsersDTO> USER_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> TOKEN_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> APP_ID_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     *
     * @param user 用户信息
     */
    public static void setCurrentUser(UsersDTO user) {
        USER_THREAD_LOCAL.set(user);
        log.debug("设置当前用户信息: userId={}", user != null ? user.getId() : null);
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    public static UsersDTO getCurrentUser() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        UsersDTO user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 设置当前请求的JWT Token
     *
     * @param token JWT Token
     */
    public static void setCurrentToken(String token) {
        TOKEN_THREAD_LOCAL.set(token);
        log.debug("设置当前请求Token");
    }

    /**
     * 获取当前请求的JWT Token
     *
     * @return JWT Token
     */
    public static String getCurrentToken() {
        return TOKEN_THREAD_LOCAL.get();
    }

    /**
     * 设置当前请求的应用ID
     *
     * @param appId 应用ID
     */
    public static void setCurrentAppId(String appId) {
        APP_ID_THREAD_LOCAL.set(appId);
        log.debug("设置当前应用ID: {}", appId);
    }

    /**
     * 获取当前请求的应用ID
     *
     * @return 应用ID
     */
    public static String getCurrentAppId() {
        return APP_ID_THREAD_LOCAL.get();
    }

    /**
     * 检查当前是否有用户登录
     *
     * @return true-已登录，false-未登录
     */
    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * 检查当前用户是否为指定用户
     *
     * @param userId 用户ID
     * @return true-是指定用户，false-不是
     */
    public static boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * 清除当前线程的用户上下文信息
     * 通常在请求结束时调用
     */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
        TOKEN_THREAD_LOCAL.remove();
        APP_ID_THREAD_LOCAL.remove();
        log.debug("清除用户上下文信息");
    }

    /**
     * 获取用户上下文摘要信息（用于日志记录）
     *
     * @return 上下文摘要
     */
    public static String getContextSummary() {
        UsersDTO user = getCurrentUser();
        String appId = getCurrentAppId();
        return String.format("userId=%s, appId=%s", 
                user != null ? user.getId() : "null", 
                appId != null ? appId : "null");
    }
}