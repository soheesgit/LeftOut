package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.IngredientDTO;
import com.example.demo.TEST_001.repository.IngredientRepository;
import com.example.demo.TEST_001.repository.UserRecipeMatchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeMatchService {

    private final UserRecipeMatchRepository matchRepository;
    private final IngredientRepository ingredientRepository;
    private final SqlSessionTemplate sql;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 사용자의 모든 레시피 매칭 점수 재계산 (비동기)
     */
    @Async
    @Transactional
    public void recalculateMatchScoresAsync(Long userId) {
        recalculateMatchScores(userId);
    }

    /**
     * 사용자의 모든 레시피 매칭 점수 재계산 (동기)
     * API 레시피 + 사용자 레시피 모두 포함
     */
    @Transactional
    public void recalculateMatchScores(Long userId) {
        log.info("사용자 {} 매칭 점수 재계산 시작 (API + 사용자 레시피)", userId);
        long startTime = System.currentTimeMillis();

        try {
            // 1. 사용자의 활성 식재료 목록 조회
            List<IngredientDTO> userIngredients = ingredientRepository.getList(userId);
            Set<String> userIngredientNames = userIngredients.stream()
                    .map(IngredientDTO::getIngredientName)
                    .map(String::trim)
                    .collect(Collectors.toSet());

            // 2. 기존 매칭 점수 삭제
            matchRepository.deleteByUserId(userId);

            // 사용자 식재료가 없으면 계산할 필요 없음
            if (userIngredientNames.isEmpty()) {
                log.info("사용자 {} 식재료 없음 - 매칭 점수 계산 스킵", userId);
                return;
            }

            // 3. 모든 레시피 조회 (API + 사용자 레시피)
            List<Map<String, Object>> recipes = matchRepository.findAllRecipeIds();

            // 4. 배치 저장용 리스트
            List<Map<String, Object>> matchList = new ArrayList<>();
            int batchSize = 100;

            for (Map<String, Object> recipe : recipes) {
                Long recipeId = ((Number) recipe.get("id")).longValue();
                String source = (String) recipe.get("source");

                Map<String, Object> matchResult;

                if ("api".equals(source)) {
                    // API 레시피: parsed_ingredients 사용
                    String parsedIngredients = (String) recipe.get("parsedIngredients");
                    matchResult = calculateMatch(parsedIngredients, userIngredientNames);
                } else {
                    // 사용자 레시피: ingredients JSON 사용
                    String ingredientsJson = (String) recipe.get("ingredients");
                    matchResult = calculateMatchForUserRecipe(ingredientsJson, userIngredientNames);
                }

                int matchedCount = (int) matchResult.get("matchedCount");
                int totalCount = (int) matchResult.get("totalCount");
                double matchPercent = (double) matchResult.get("matchPercent");
                String matchedIngredientStr = (String) matchResult.get("matchedIngredients");

                // 매칭된 재료가 있는 경우만 저장 (저장 공간 최적화)
                if (matchedCount > 0) {
                    Map<String, Object> matchData = new HashMap<>();
                    matchData.put("userId", userId);
                    matchData.put("recipeId", recipeId);
                    matchData.put("matchedCount", matchedCount);
                    matchData.put("totalCount", totalCount);
                    matchData.put("matchPercent", matchPercent);
                    matchData.put("matchedIngredients", matchedIngredientStr);
                    matchList.add(matchData);
                }

                // 배치 저장
                if (matchList.size() >= batchSize) {
                    matchRepository.batchSave(matchList);
                    matchList.clear();
                }
            }

            // 남은 데이터 저장
            if (!matchList.isEmpty()) {
                matchRepository.batchSave(matchList);
            }

            long endTime = System.currentTimeMillis();
            log.info("사용자 {} 매칭 점수 재계산 완료 - {}ms 소요, {} 레시피 처리",
                    userId, (endTime - startTime), recipes.size());

        } catch (Exception e) {
            log.error("사용자 {} 매칭 점수 재계산 실패", userId, e);
        }
    }

    /**
     * 매칭 점수 계산
     */
    private Map<String, Object> calculateMatch(String parsedIngredients, Set<String> userIngredientNames) {
        Map<String, Object> result = new HashMap<>();
        Set<String> recipeIngredientSet = new HashSet<>();

        // JSON 파싱
        if (parsedIngredients != null && !parsedIngredients.isEmpty() && !parsedIngredients.equals("[]")) {
            try {
                JsonNode jsonArray = objectMapper.readTree(parsedIngredients);
                if (jsonArray.isArray()) {
                    for (JsonNode node : jsonArray) {
                        String ingredient = node.asText().trim();
                        if (!ingredient.isEmpty()) {
                            recipeIngredientSet.add(ingredient);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("JSON 파싱 실패: {}", e.getMessage());
            }
        }

        int totalCount = recipeIngredientSet.size();
        if (totalCount == 0) {
            result.put("matchedCount", 0);
            result.put("totalCount", 0);
            result.put("matchPercent", 0.0);
            result.put("matchedIngredients", "");
            return result;
        }

        int matchedCount = 0;
        List<String> matchedIngredients = new ArrayList<>();

        for (String recipeIngredient : recipeIngredientSet) {
            // 완전 일치
            if (userIngredientNames.contains(recipeIngredient)) {
                matchedCount++;
                matchedIngredients.add(recipeIngredient);
                continue;
            }

            // 부분 일치
            for (String userIngredient : userIngredientNames) {
                if (recipeIngredient.contains(userIngredient) || userIngredient.contains(recipeIngredient)) {
                    matchedCount++;
                    matchedIngredients.add(userIngredient + "(부분)");
                    break;
                }
            }
        }

        double matchPercent = totalCount > 0 ? (matchedCount * 100.0 / totalCount) : 0;

        result.put("matchedCount", matchedCount);
        result.put("totalCount", totalCount);
        result.put("matchPercent", Math.round(matchPercent * 100.0) / 100.0);
        result.put("matchedIngredients", String.join(", ", matchedIngredients));

        return result;
    }

    /**
     * 사용자의 매칭 점수가 계산되어 있는지 확인
     */
    public boolean hasMatchScores(Long userId) {
        return matchRepository.countByUserId(userId) > 0;
    }

    /**
     * 사용자 레시피용 매칭 계산
     * ingredients JSON: [{"name":"김치","amount":"200g"}, ...]
     */
    private Map<String, Object> calculateMatchForUserRecipe(String ingredientsJson, Set<String> userIngredientNames) {
        Map<String, Object> result = new HashMap<>();
        Set<String> recipeIngredientSet = new HashSet<>();

        // JSON 파싱 (사용자 레시피 형식)
        if (ingredientsJson != null && !ingredientsJson.isEmpty() && !ingredientsJson.equals("[]")) {
            try {
                JsonNode jsonArray = objectMapper.readTree(ingredientsJson);
                if (jsonArray.isArray()) {
                    for (JsonNode node : jsonArray) {
                        // 사용자 레시피 형식: {"name": "재료명", "amount": "양"}
                        String name = node.path("name").asText().trim();
                        if (!name.isEmpty()) {
                            recipeIngredientSet.add(name);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("사용자 레시피 ingredients JSON 파싱 실패: {}", e.getMessage());
            }
        }

        int totalCount = recipeIngredientSet.size();
        if (totalCount == 0) {
            result.put("matchedCount", 0);
            result.put("totalCount", 0);
            result.put("matchPercent", 0.0);
            result.put("matchedIngredients", "");
            return result;
        }

        int matchedCount = 0;
        List<String> matchedIngredients = new ArrayList<>();

        for (String recipeIngredient : recipeIngredientSet) {
            // 완전 일치
            if (userIngredientNames.contains(recipeIngredient)) {
                matchedCount++;
                matchedIngredients.add(recipeIngredient);
                continue;
            }

            // 부분 일치
            for (String userIngredient : userIngredientNames) {
                if (recipeIngredient.contains(userIngredient) || userIngredient.contains(recipeIngredient)) {
                    matchedCount++;
                    matchedIngredients.add(userIngredient + "(부분)");
                    break;
                }
            }
        }

        double matchPercent = totalCount > 0 ? (matchedCount * 100.0 / totalCount) : 0;

        result.put("matchedCount", matchedCount);
        result.put("totalCount", totalCount);
        result.put("matchPercent", Math.round(matchPercent * 100.0) / 100.0);
        result.put("matchedIngredients", String.join(", ", matchedIngredients));

        return result;
    }
}
