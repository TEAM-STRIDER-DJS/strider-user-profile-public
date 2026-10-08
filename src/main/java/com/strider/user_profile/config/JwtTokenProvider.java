package com.strider.user_profile.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.access-secret-key}")
    private String accessSecret;

    @Value("${jwt.refresh-secret-key}")
    private String refreshSecret;

    private Key accessSigningKey;
    private Key refreshSigningKey;

    private static final long REFRESH_TOKEN_EXPIRATION_TIME_MS = 1000L * 60 * 60 * 24 * 30; // 30 days
    private static final long ACCESS_TOKEN_EXPIRATION_TIME_MS = 1000 * 60 * 60 * 24; // 1 day

    @PostConstruct
    public void init() {
        this.accessSigningKey = Keys.hmacShaKeyFor(accessSecret.getBytes());
        this.refreshSigningKey = Keys.hmacShaKeyFor(refreshSecret.getBytes());
//        this.accessSigningKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(accessSecret));     // yml에 base64 인코딩된 키
//        this.refreshSigningKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(refreshSecret));
    }

    public String createAccessToken(String userId) {
        Date now = new Date();
        Date expired_date = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME_MS);

        Map<String, String> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("type", "access");

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expired_date)
                .signWith(accessSigningKey, SignatureAlgorithm.HS256)
                .setClaims(claims)
                .compact();
    }

    public String createRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME_MS); // 14일

        Map<String, String> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("type", "refresh");

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(refreshSigningKey, SignatureAlgorithm.HS256)
                .setClaims(claims)
                .compact();
    }

    public String getUserIdFromAccessToken(String token) {
        return getUserIdFromToken(token, accessSigningKey);
    }

    public String getUserIdFromRefreshToken(String token) {
        return getUserIdFromToken(token, refreshSigningKey);
    }

    private String getUserIdFromToken(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateAccessToken(String accessToken){
        return validateToken(accessToken, accessSigningKey);
    }

    public boolean validateRefreshToken(String refreshToken){
        return validateToken(refreshToken, refreshSigningKey);
    }

    public boolean validateToken(String token, Key signingKey){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token.substring(7));
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Invalid token: {}", e.getMessage());
        }
        return false;
    }
}
