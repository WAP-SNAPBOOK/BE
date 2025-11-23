package com.example.easybooking.chat.config;

import java.util.Collections;

import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.easybooking.auth.util.JwtUtil;
import com.example.easybooking.common.filter.TraceIdFilter;
import com.example.easybooking.errors.exception.AuthException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    /**
     * Intercepts STOMP CONNECT frames to validate a Bearer JWT and, on success, attach a Spring Security
     * Authentication to the STOMP session; non-CONNECT frames are passed through unchanged.
     *
     * @param message the incoming STOMP message (CONNECT frames are inspected for an `Authorization: Bearer <token>` header)
     * @param channel the message channel through which the message was received
     * @return the original `message` when authentication succeeds or the message is not a CONNECT; `null` to reject the CONNECT when the Authorization header is missing, malformed, or the token is invalid or fails processing
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        final String traceId = currentTraceId();

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            String sessionId = accessor.getSessionId();
            String destination = accessor.getDestination();

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.info("[{}][session={}] WebSocket JWT 토큰 발견 preview={}",
                        traceId,
                        sessionId,
                        token.substring(0, Math.min(20, token.length())) + "...");
                try {
                    // JWT 토큰 검증
                    if (!jwtUtil.validateToken(token)) {
                        log.warn("[{}][session={}] WebSocket JWT 검증 실패 reason=invalid-token",
                                traceId, sessionId);
                        return null; // CONNECT 차단
                    }

                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);

                    log.info("[{}][session={}] WebSocket JWT 검증 성공 userId={} role={}",
                            traceId, sessionId, userId, role);

                    // Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    // STOMP 세션에 인증 정보 저장
                    accessor.setUser(authentication);

                    log.info("[{}][session={}] WebSocket 인증 성공 destination={}",
                            traceId, sessionId, destination);
                } catch (AuthException e) {
                    log.warn("[{}][session={}] WebSocket JWT 실패 code={} msg={} destination={}",
                            traceId,
                            sessionId,
                            e.getErrorCode().name(),
                            e.getMessage(),
                            destination,
                            e);
                    return null; // CONNECT 차단
                } catch (Exception e) {
                    log.error("[{}][session={}] WebSocket JWT 처리 중 예상치 못한 오류 destination={}",
                            traceId,
                            sessionId,
                            destination,
                            e);
                    return null; // CONNECT 차단
                }
            } else {
                log.warn("[{}][session={}] WebSocket JWT 실패 reason=missing-authorization",
                        traceId, sessionId);
                return null; // CONNECT 차단
            }
        }

        return message;
    }

    /**
     * Logs the destination of a STOMP SUBSCRIBE frame when a message is sent.
     *
     * If the message contains a STOMP SUBSCRIBE command, an info-level log entry is written
     * with the subscription destination for monitoring and auditing purposes.
     */
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            log.info("SUBSCRIBE: {}", accessor.getDestination());
        }
    }

    /**
     * Get the current trace identifier from MDC, or "no-trace" when none is available.
     *
     * @return the trace identifier stored under TraceIdFilter.TRACE_ID_KEY in MDC, or `"no-trace"` if not present
     */
    private String currentTraceId() {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        return traceId != null ? traceId : "no-trace";
    }

}