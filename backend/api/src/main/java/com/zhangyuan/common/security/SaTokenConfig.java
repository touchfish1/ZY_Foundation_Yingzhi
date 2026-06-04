package com.zhangyuan.common.security;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.zhangyuan.modules.auth.SaTokenSecurityContextBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private final SaTokenSecurityContextBridge contextBridge;

    public SaTokenConfig(SaTokenSecurityContextBridge contextBridge) {
        this.contextBridge = contextBridge;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/admin/**")
                .notMatch("/admin/auth/login")
                .check(r -> StpUtil.checkLogin());
            SaRouter.match("/api/**", "/actuator/**").check(r -> {});
        })).addPathPatterns("/**");
        registry.addInterceptor(contextBridge).addPathPatterns("/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
