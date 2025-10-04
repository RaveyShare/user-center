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
    public String generateToken(Long userId, Long appId) {
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
     * 从令牌中获取应用ID
     *
     * @param token JWT令牌
     * @return 应用ID
     */
    public Long getAppIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object appId = claims.get("appId");
            if (appId instanceof Integer) {
                return ((Integer) appId).longValue();
            } else if (appId instanceof Long) {
                return (Long) appId;
            }
        }
        return null;
    }
}