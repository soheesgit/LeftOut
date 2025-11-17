package com.example.demo.TEST_001.config;

import com.example.demo.TEST_001.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 모든 경로에 인터셉터 적용
                .excludePathPatterns(
                        "/",               // 홈페이지
                        "/login",          // 로그인 페이지
                        "/signup",         // 회원가입 페이지
                        "/logout",         // 로그아웃
                        "/css/**",         // CSS 파일
                        "/js/**",          // JS 파일
                        "/images/**",      // 이미지 파일
                        "/error"           // 에러 페이지
                );
    }
}
