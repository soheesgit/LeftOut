package com.example.demo.TEST_001.config;

import com.example.demo.TEST_001.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 연결 타임아웃: 5초
        factory.setReadTimeout(10000);   // 읽기 타임아웃: 10초
        return new RestTemplate(factory);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 이미지 파일 서빙 (상위 폴더 기준)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./src/main/resources/static/uploads/",
                                      "file:../src/main/resources/static/uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 모든 경로에 인터셉터 적용
                .excludePathPatterns(
                        "/",                          // 홈페이지
                        "/login",                     // 로그인 페이지
                        "/signup",                    // 회원가입 페이지
                        "/logout",                    // 로그아웃
                        "/notification/subscribe",    // SSE 알림 구독
                        "/notification/unread-count", // 안읽은 알림 개수
                        "/recipe/all-recipes",        // 전체 레시피 목록 (공개)
                        "/recipe/user-recipe/**",     // 레시피 상세보기 (공개)
                        "/css/**",                    // CSS 파일
                        "/js/**",                     // JS 파일
                        "/images/**",                 // 이미지 파일
                        "/uploads/**",                // 업로드된 파일
                        "/error"                      // 에러 페이지
                );
    }
}
