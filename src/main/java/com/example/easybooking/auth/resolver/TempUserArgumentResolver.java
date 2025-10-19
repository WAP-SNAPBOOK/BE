package com.example.easybooking.auth.resolver;

import com.example.easybooking.auth.RequireTempUser;
import com.example.easybooking.auth.TempUser;
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
 * @RequireTempUser 애너테이션이 붙은 파라미터를 자동으로 해석하는 Resolver
 * SecurityContext에서 TempUser를 추출하고 검증합니다.
 */
@Component
@Slf4j
public class TempUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(RequireTempUser.class);
        boolean isTempUserType = parameter.getParameterType().equals(TempUser.class);
        return hasAnnotation && isTempUserType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                 ModelAndViewContainer mavContainer,
                                 NativeWebRequest webRequest,
                                 WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getPrincipal() == null) {
            log.warn("인증 정보가 없습니다.");
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        
        Object principal = authentication.getPrincipal();

        if (principal instanceof TempUser tempUser) {
            log.debug("TempUser 추출 성공: providerId={}", tempUser.getProviderId());
            return tempUser;
        }

        log.warn("TempUser가 아닌 principal: {}", principal.getClass().getSimpleName());
        throw new IllegalArgumentException("회원가입 토큰이 필요합니다. 이미 가입된 사용자이거나 잘못된 토큰입니다.");
    }
}

