package com.ravey.ai.user.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravey.ai.user.service.context.UserContext;
import com.ravey.ai.user.api.dto.UsersDTO;
import com.ravey.ai.user.service.cache.CacheService;
import com.ravey.ai.user.api.service.UsersService;
import com.ravey.ai.user.api.utils.JwtUtils;
import com.ravey.common.service.web.result.HttpResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 小程序Token过滤器
 * 主要作用是校验JWT token，登录接口不拦截
 *
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MiniAppTokenFilter implements Filter {

    private final JwtUtils jwtUtils;
    private final CacheService cacheService;
    private final UsersService usersService;
    private final ObjectMapper objectMapper;

    /**
     * 不需要token验证的接口路径
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/front/auth/wxMiniAppLogin",
            "/front/auth/qr/generate",
            "/front/auth/qr/check",
            "/front/auth/qr/wxacode",
            "/api/front/auth/qr/generate",
            "/api/front/auth/qr/check",
            "/api/front/auth/qr/wxacode",
            "/health",
            "/actuator",
            "/swagger",
            "/v3/api-docs",
            "/favicon.ico"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("MiniAppTokenFilter 初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String requestURI = httpRequest.getRequestURI();
            log.debug("处理请求: {}", requestURI);

            // 检查是否为排除路径
            if (isExcludePath(requestURI)) {
                log.debug("跳过token验证: {}", requestURI);
                chain.doFilter(request, response);
                return;
            }

            // 获取token
            String token = extractToken(httpRequest);
            if (!StringUtils.hasText(token)) {
                log.warn("请求缺少token: {}", requestURI);
                writeErrorResponse(httpResponse, "缺少认证token", HttpStatus.UNAUTHORIZED);
                return;
            }

            // 验证token格式和有效性
            if (!jwtUtils.validateToken(token)) {
                log.warn("token验证失败: {}", requestURI);
                writeErrorResponse(httpResponse, "token无效或已过期", HttpStatus.UNAUTHORIZED);
                return;
            }

            // 从token中获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("无法从token中获取用户ID: {}", requestURI);
                writeErrorResponse(httpResponse, "token格式错误", HttpStatus.UNAUTHORIZED);
                return;
            }

            // 检查会话缓存
            Long cachedUserId = cacheService.getUserSession(token);
            if (cachedUserId == null || !cachedUserId.equals(userId)) {
                log.warn("会话已失效或用户ID不匹配: tokenUserId={}, cachedUserId={}", userId, cachedUserId);
                writeErrorResponse(httpResponse, "会话已失效，请重新登录", HttpStatus.UNAUTHORIZED);
                return;
            }

            // 获取用户信息
            UsersDTO user = getUserInfo(userId);
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                writeErrorResponse(httpResponse, "用户不存在", HttpStatus.UNAUTHORIZED);
                return;
            }

            // 设置用户上下文
            UserContext.setCurrentUser(user);
            UserContext.setCurrentToken(token);
            
            // 从token中获取appId并设置到上下文
            String appId = jwtUtils.getAppIdFromToken(token);
            if (StringUtils.hasText(appId)) {
                UserContext.setCurrentAppId(appId);
            }

            log.debug("用户认证成功: userId={}, appId={}", userId, appId);

            // 继续处理请求
            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Token过滤器处理异常", e);
            writeErrorResponse(httpResponse, "认证服务异常", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // 清理用户上下文
            UserContext.clear();
        }
    }

    @Override
    public void destroy() {
        log.info("MiniAppTokenFilter 销毁完成");
    }

    /**
     * 检查是否为排除路径
     *
     * @param requestURI 请求URI
     * @return 是否排除
     */
    private boolean isExcludePath(String requestURI) {
        return EXCLUDE_PATHS.stream().anyMatch(requestURI::startsWith);
    }

    /**
     * 从请求中提取token
     *
     * @param request HTTP请求
     * @return token
     */
    private String extractToken(HttpServletRequest request) {
        // 优先从Header中获取
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 从Header中获取token字段
        String tokenHeader = request.getHeader("token");
        if (StringUtils.hasText(tokenHeader)) {
            return tokenHeader;
        }

        // 从参数中获取
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }

    /**
     * 获取用户信息（优先从缓存获取）
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    private UsersDTO getUserInfo(Long userId) {
        try {
            // 先从缓存获取
            UsersDTO cachedUser = cacheService.getUserInfo(userId);
            if (cachedUser != null) {
                log.debug("从缓存获取用户信息: userId={}", userId);
                return cachedUser;
            }

            // 缓存中没有，从数据库获取
            UsersDTO user = usersService.getById(userId);
            if (user != null) {
                // 缓存用户信息
                cacheService.cacheUserInfo(user);
                log.debug("从数据库获取用户信息并缓存: userId={}", userId);
            }
            return user;
        } catch (Exception e) {
            log.error("获取用户信息失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 写入错误响应
     *
     * @param response HTTP响应
     * @param message  错误消息
     * @param status   HTTP状态码
     */
    private void writeErrorResponse(HttpServletResponse response, String message, HttpStatus status) {
        try {
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            HttpResult<Object> result = HttpResult.failure(status.value(), message);
            String jsonResponse = objectMapper.writeValueAsString(result);
            
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("写入错误响应失败", e);
        }
    }
}