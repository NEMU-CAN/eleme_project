package com.iteleme.backend.config;

// ============================================================
// [阶段① 新增] 注册鉴权拦截器到 /api/**
// 说明：单独一个 WebMvcConfigurer，与 CorsConfig 并存（Spring 会收集所有实现）。
// ============================================================
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AuthConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    // public AuthConfig(AuthInterceptor authInterceptor) {
    //     this.authInterceptor = authInterceptor;
    // }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**");
    }
}
