package com.iteleme.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 跨域配置。
 *
 * <p>放行 {@code /api/**} 的跨域请求，允许任意来源、常用方法与任意请求头，
 * 为前端联调扫清跨域障碍。应用无 Cookie/Session 认证（登录不返回 token），
 * 故不开启 {@code allowCredentials}，避免与 {@code allowedOrigins("*")} 冲突。</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 为 {@code /api/**} 注册跨域映射。
     *
     * @param registry CORS 注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
