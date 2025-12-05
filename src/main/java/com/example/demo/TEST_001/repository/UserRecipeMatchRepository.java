package com.example.demo.TEST_001.repository;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserRecipeMatchRepository {
    private final SqlSessionTemplate sql;

    // 매칭 점수 저장/업데이트 (UPSERT)
    public void saveOrUpdate(Long userId, Long recipeId, int matchedCount, int totalCount, double matchPercent, String matchedIngredients) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("recipeId", recipeId);
        params.put("matchedCount", matchedCount);
        params.put("totalCount", totalCount);
        params.put("matchPercent", matchPercent);
        params.put("matchedIngredients", matchedIngredients);
        sql.insert("userRecipeMatch.saveOrUpdate", params);
    }

    // 사용자의 모든 매칭 점수 삭제 (재계산 전)
    public void deleteByUserId(Long userId) {
        sql.delete("userRecipeMatch.deleteByUserId", userId);
    }

    // 배치 저장 (성능 최적화)
    public void batchSave(List<Map<String, Object>> matchList) {
        if (matchList != null && !matchList.isEmpty()) {
            sql.insert("userRecipeMatch.batchSave", matchList);
        }
    }

    // 매칭 점수 조회 개수
    public int countByUserId(Long userId) {
        Integer count = sql.selectOne("userRecipeMatch.countByUserId", userId);
        return count != null ? count : 0;
    }

    // ========================================
    // 통합 레시피 조회 (API + 사용자)
    // ========================================

    // 모든 레시피 ID 조회 (매칭 계산용 - API + 사용자)
    public List<Map<String, Object>> findAllRecipeIds() {
        return sql.selectList("userRecipeMatch.findAllRecipeIds");
    }

    // 통합 레시피 목록 조회 (매칭 점수 포함)
    public List<com.example.demo.TEST_001.dto.UserRecipeDTO> findIntegratedRecipesWithMatch(
            Long userId, String source, String rcpWay2, String rcpPat2,
            String searchRecipeName, String searchIngredient, String searchAuthor,
            int limit, int offset) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("source", source);
        params.put("rcpWay2", rcpWay2);
        params.put("rcpPat2", rcpPat2);
        params.put("searchRecipeName", searchRecipeName);
        params.put("searchIngredient", searchIngredient);
        params.put("searchAuthor", searchAuthor);
        params.put("limit", limit);
        params.put("offset", offset);
        return sql.selectList("userRecipeMatch.findIntegratedRecipesWithMatch", params);
    }

    // 통합 레시피 개수 조회
    public int countIntegratedRecipesFiltered(String source, String rcpWay2, String rcpPat2,
                                               String searchRecipeName, String searchIngredient,
                                               String searchAuthor) {
        Map<String, Object> params = new HashMap<>();
        params.put("source", source);
        params.put("rcpWay2", rcpWay2);
        params.put("rcpPat2", rcpPat2);
        params.put("searchRecipeName", searchRecipeName);
        params.put("searchIngredient", searchIngredient);
        params.put("searchAuthor", searchAuthor);
        Integer count = sql.selectOne("userRecipeMatch.countIntegratedRecipesFiltered", params);
        return count != null ? count : 0;
    }
}
