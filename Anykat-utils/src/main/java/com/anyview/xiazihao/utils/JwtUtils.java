package com.anyview.xiazihao.utils;

import com.anyview.xiazihao.config.AppConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtils {
    private static final String SECRET_KEY; // 秘钥
    private static final long EXPIRATION_TIME;
    private static final String ADMIN_SECRET_KEY;

    static {
        try {
            SECRET_KEY = AppConfig.getInstance().getJwt().getSecretKey();
            ADMIN_SECRET_KEY = AppConfig.getInstance().getJwt().getAdminSecretKey();
            EXPIRATION_TIME = AppConfig.getInstance().getJwt().getExpireTime();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成JWT令牌
     */
    public static String createToken(Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(claims) // 添加 Claims
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    /**
     * 生成管理员JWT令牌
     */
    public static String createAdminToken(Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(ADMIN_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(claims) // 添加 Claims
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    /**
     * 解析JWT令牌
     */
    public static Claims parseToken(String token) throws JwtException {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key) // 设置签名密钥
                .build()
                .parseSignedClaims(token) // 解析 JWT
                .getPayload(); // 获取 Claims
    }

    /**
     * 解析管理员JWT令牌
     */
    public static Claims parseAdminToken(String token) throws JwtException {
        SecretKey key = Keys.hmacShaKeyFor(ADMIN_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key) // 设置签名密钥
                .build()
                .parseSignedClaims(token) // 解析 JWT
                .getPayload(); // 获取 Claims
    }

    /**
     * 验证JWT令牌的userId字段
     */
    public static void verifyUserId(String token, Integer userId) throws JwtException {
        Claims claims = parseToken(token);
        Integer id = (Integer) claims.get("id");
        if (!id.equals(userId)) {
            throw new SignatureException("Token中的userId与请求参数不一致");
        }
    }
}