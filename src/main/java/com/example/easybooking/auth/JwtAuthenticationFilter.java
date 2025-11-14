package com.example.easybooking.auth;

import com.example.easybooking.auth.domain.AuthPrincipal;
import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.auth.domain.TempUser;
import com.example.easybooking.auth.util.JwtUtil;
import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            jwtUtil.validateToken(token);

            String role = jwtUtil.getRoleFromToken(token);
            AuthPrincipal principal;
            GrantedAuthority authority;

            // TEMP 토큰인지 확인
            if ("TEMP".equals(role)) {
                String providerId = jwtUtil.getSubjectFromToken(token);
                principal = new TempUser(providerId);
                authority = new SimpleGrantedAuthority("ROLE_TEMP");
                log.info("TEMP 토큰 인증 성공: providerId={}", providerId);
            } else {
                Long userId = jwtUtil.getUserIdFromToken(token);
                principal = new AuthenticatedUser(userId, role);
                authority = new SimpleGrantedAuthority("ROLE_" + role);
                log.info("정식 토큰 인증 성공: userId={}, role={}", userId, role);
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            Collections.singletonList(authority)
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (AuthException ex) {
            log.warn("토큰 인증 실패: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("토큰 인증 중 예상치 못한 오류 발생: {}", ex.getMessage());
            throw new AuthException(AuthErrorCode.INTERNAL_SEVERVER_ERROR);
        }
    }

    // HTTP 헤더에서 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}