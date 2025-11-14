package com.example.easybooking.auth.util;

import com.example.easybooking.auth.dto.AuthTokens;
import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
                   @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration; // 1시간
        this.refreshTokenExpiration = refreshTokenExpiration; // 7일
    }

    public AuthTokens generateTokens(Long userId, String role) {
        try {
            String accessToken = generateAccessToken(userId, role);
            String refreshToken = generateRefreshToken(userId);
            return new AuthTokens(accessToken, refreshToken);
        } catch (Exception e) {
            log.error("토큰 생성 중 오류 발생: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.TOKEN_CREATION_FAILED);
        }

    }

    // Access Token 생성
    public String generateAccessToken(Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String generateTempToken(String providerId) {
        return Jwts.builder()
                .setSubject(providerId)
                .claim("type", "TEMP")  // 임시 토큰 표시
                .claim("role", "TEMP")  // 임시 role 추가
                .setExpiration(new Date(System.currentTimeMillis() + 600000)) // 10분 유효
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    public String getSubjectFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();  // String 그대로 반환
    }

    // 토큰에서 역할 추출
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        parseToken(token);
        return true;
    }

    // 토큰 파싱
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰입니다: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.UNSUPPORTED_TOKEN);
        } catch (io.jsonwebtoken.MalformedJwtException | io.jsonwebtoken.security.SecurityException e) {
            log.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("비어있거나 잘못된 JWT 토큰입니다: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        } catch (JwtException e) {
            log.error("JWT 토큰 파싱 중 알 수 없는 오류: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.TOKEN_PARSING_FAILED);
        }
    }

    public String getTokenType(String refreshToken) {
        Claims claims = parseToken(refreshToken);
        return claims.get("type", String.class);
    }
}
