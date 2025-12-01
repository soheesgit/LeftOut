package com.example.demo.TEST_001.config;

import com.example.demo.TEST_001.service.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 앱 시작시 API 레시피 데이터를 DB에 동기화하는 초기화 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecipeDataInitializer implements ApplicationRunner {

    private final RecipeService recipeService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("========================================");
        log.info("레시피 데이터 초기화 시작...");
        log.info("========================================");

        try {
            recipeService.syncApiRecipes();
            log.info("레시피 데이터 초기화 완료!");
        } catch (Exception e) {
            log.error("레시피 데이터 초기화 중 오류 발생", e);
        }
    }
}
