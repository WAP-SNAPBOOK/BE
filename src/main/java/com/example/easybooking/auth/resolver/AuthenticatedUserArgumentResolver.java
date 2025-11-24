package com.example.easybooking.auth.resolver;

import com.example.easybooking.auth.annotation.RequireAuthenticatedUser;
import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import com.example.easybooking.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @RequireAuthenticatedUser 애너테이션이 붙은 파라미터를 자동으로 해석하는 Resolver
 * SecurityContext에서 AuthenticatedUser를 추출하고 검증합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(RequireAuthenticatedUser.class);
        boolean isAuthenticatedUserType = parameter.getParameterType().equals(AuthenticatedUser.class);
        return hasAnnotation && isAuthenticatedUserType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                 ModelAndViewContainer mavContainer,
                                 NativeWebRequest webRequest,
                                 WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getPrincipal() == null) {
            log.warn("인증 정보가 없습니다.");
            throw new AuthException(AuthErrorCode.LOGIN_REQUIRED);
        }
        
        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedUser user) {
            if (!userRepository.existsById(user.getUserId())) {
                log.warn("토큰은 유효하지만 DB에 존재하지 않는 사용자입니다. userId={}", user.getUserId());
                throw new AuthException(AuthErrorCode.FULL_LOGIN_REQUIRED);
            }
            log.debug("AuthenticatedUser 추출 성공: userId={}, role={}", user.getUserId(), user.getRole());
            return user;
        }

        log.warn("AuthenticatedUser가 아닌 principal: {}", principal.getClass().getSimpleName());
        throw new AuthException(AuthErrorCode.FULL_LOGIN_REQUIRED);
    }
}

