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
}
