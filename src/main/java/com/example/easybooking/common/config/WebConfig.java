package com.example.easybooking.common.config;

import com.example.easybooking.auth.resolver.AuthenticatedUserArgumentResolver;
import com.example.easybooking.auth.resolver.TempUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 설정
 * Custom ArgumentResolver를 등록합니다.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final TempUserArgumentResolver tempUserArgumentResolver;
    private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;
    
    /**
     * Custom ArgumentResolver 등록
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(tempUserArgumentResolver);
        resolvers.add(authenticatedUserArgumentResolver);
    }
}

