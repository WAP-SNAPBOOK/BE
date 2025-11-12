package com.example.easybooking.chat.config;

import com.example.easybooking.auth.util.JwtUtil;
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
                    if (jwtUtil.validateToken(token)) {
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
                    } else {
                        log.warn("JWT 토큰 검증 실패");
                        throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
                    }
                } catch (Exception e) {
                    log.error("JWT 토큰 검증 중 오류 발생: {}", e.getMessage());
                    throw new IllegalArgumentException("토큰 검증 실패: " + e.getMessage());
                }
            } else {
                log.warn("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
                throw new IllegalArgumentException("인증 토큰이 필요합니다.");
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