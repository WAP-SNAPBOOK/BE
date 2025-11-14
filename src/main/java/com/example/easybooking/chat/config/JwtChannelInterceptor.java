package com.example.easybooking.chat.config;

import com.example.easybooking.auth.util.JwtUtil;
import com.example.easybooking.errors.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.info("JWT 토큰 발견: {}", token.substring(0, Math.min(20, token.length())) + "...");
                try {
                    // JWT 토큰 검증
                    if (!jwtUtil.validateToken(token)) {
                        log.warn("JWT 토큰 검증 실패 - 유효하지 않은 토큰");
                        return null; // CONNECT 차단
                    }

                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);

                    log.info("JWT 검증 성공 - UserId: {}, Role: {}", userId, role);

                    // Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    // STOMP 세션에 인증 정보 저장
                    accessor.setUser(authentication);

                    log.info("WebSocket 인증 성공!");
                } catch (AuthException e) {
                    log.warn("JWT 토큰 검증 실패(AuthException): {}", e.getAuthErrorCode().getMessage());
                    return null; // CONNECT 차단
                } catch (Exception e) {
                    log.error("JWT 토큰 검증 중 예상치 못한 오류 발생: {}", e.getMessage());
                    return null; // CONNECT 차단
                }
            } else {
                log.warn("Authorization 헤더가 없거나 형식이 잘못되었습니다. WebSocket 인증 거부");
                return null; // CONNECT 차단
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            log.info("SUBSCRIBE: {}", accessor.getDestination());
        }
    }
}