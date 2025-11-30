package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.RecipeLikeDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RecipeLikeRepository {
    private final SqlSessionTemplate sql;

    // 좋아요 추가
    public void save(RecipeLikeDTO likeDTO) {
        sql.insert("recipeLike.save", likeDTO);
    }

    // 좋아요 삭제
    public void deleteByRecipeIdAndUserId(Long recipeId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("recipeId", recipeId);
        params.put("userId", userId);
        sql.delete("recipeLike.deleteByRecipeIdAndUserId", params);
    }

    // 좋아요 존재 여부 확인
    public boolean existsByRecipeIdAndUserId(Long recipeId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("recipeId", recipeId);
        params.put("userId", userId);
        Integer count = sql.selectOne("recipeLike.existsByRecipeIdAndUserId", params);
        return count != null && count > 0;
    }

    // 레시피별 좋아요 수 조회
    public int countByRecipeId(Long recipeId) {
        Integer count = sql.selectOne("recipeLike.countByRecipeId", recipeId);
        return count != null ? count : 0;
    }
}
