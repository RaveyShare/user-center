package com.ravey.ai.user.api.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * JWT密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT过期时间（秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成JWT令牌
     *
     * @param userId 用户ID
     * @param appId  应用ID
     * @return JWT令牌
     */
    public String generateToken(Long userId, String appId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("appId", appId);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 生成JWT令牌（重载方法，支持Long类型appId）
     *
     * @param userId 用户ID
     * @param appId  应用ID
     * @return JWT令牌
     */
    public String generateToken(Long userId, Long appId) {
        return generateToken(userId, String.valueOf(appId));
    }



    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return 声明
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析JWT令牌失败: {}", e.getMessage());
            throw new RuntimeException("无效的JWT令牌");
        }
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            log.error("验证JWT令牌失败", e);
            return false;
        }
    }

    /**
     * 检查令牌是否过期
     *
     * @param claims 声明
     * @return 是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }

    /**
     * 从令牌中获取应用ID（字符串格式）
     *
     * @param token JWT令牌
     * @return 应用ID
     */
    public String getAppIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object appId = claims.get("appId");
            if (appId != null) {
                return appId.toString();
            }
        }
        return null;
    }

    /**
     * 从令牌中获取应用ID（Long格式）
     *
     * @param token JWT令牌
     * @return 应用ID
     */
    public Long getAppIdAsLongFromToken(String token) {
        String appId = getAppIdFromToken(token);
        if (appId != null) {
            try {
                return Long.valueOf(appId);
            } catch (NumberFormatException e) {
                log.warn("无法将appId转换为Long: {}", appId);
            }
        }
        return null;
    }

    /**
     * 获取令牌的过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 获取令牌的签发时间
     *
     * @param token JWT令牌
     * @return 签发时间
     */
    public Date getIssuedAtFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getIssuedAt() : null;
    }

    /**
     * 检查令牌是否即将过期（在指定分钟内过期）
     *
     * @param token JWT令牌
     * @param minutes 分钟数
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, int minutes) {
        try {
            Date expiration = getExpirationFromToken(token);
            if (expiration == null) {
                return true;
            }
            
            Date now = new Date();
            long timeDiff = expiration.getTime() - now.getTime();
            long minutesLeft = timeDiff / (1000 * 60);
            
            return minutesLeft <= minutes;
        } catch (Exception e) {
            log.error("检查令牌过期时间失败", e);
            return true;
        }
    }

    /**
     * 刷新令牌（生成新的令牌，保持相同的用户信息）
     *
     * @param token 原始令牌
     * @return 新的令牌
     */
    public String refreshToken(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            String appId = getAppIdFromToken(token);
            
            if (userId != null && appId != null) {
                return generateToken(userId, appId);
            }
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
        }
        return null;
    }

    /**
     * 获取令牌的剩余有效时间（秒）
     *
     * @param token JWT令牌
     * @return 剩余有效时间（秒），如果已过期返回0
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            if (expiration == null) {
                return 0;
            }
            
            Date now = new Date();
            long timeDiff = expiration.getTime() - now.getTime();
            
            return Math.max(0, timeDiff / 1000);
        } catch (Exception e) {
            log.error("获取令牌剩余时间失败", e);
            return 0;
        }
    }
}