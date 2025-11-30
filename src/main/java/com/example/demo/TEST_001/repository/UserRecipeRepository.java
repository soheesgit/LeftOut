package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.UserRecipeDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserRecipeRepository {
    private final SqlSessionTemplate sql;

    // 레시피 생성
    public void save(UserRecipeDTO recipeDTO) {
        sql.insert("userRecipe.save", recipeDTO);
    }

    // 레시피 조회 (ID로)
    public UserRecipeDTO findById(Long id) {
        return sql.selectOne("userRecipe.findById", id);
    }

    // 레시피 조회 (ID + 좋아요 여부 확인용)
    public UserRecipeDTO findByIdWithLikeStatus(Long id, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("userId", userId);
        return sql.selectOne("userRecipe.findByIdWithLikeStatus", params);
    }

    // 전체 레시피 목록 조회 (페이징, 검색)
    public List<UserRecipeDTO> findAll(String search, Integer limit, Integer offset) {
        Map<String, Object> params = new HashMap<>();
        params.put("search", search);
        params.put("limit", limit);
        params.put("offset", offset);
        return sql.selectList("userRecipe.findAll", params);
    }

    // 사용자별 레시피 목록 조회
    public List<UserRecipeDTO> findByUserId(Long userId) {
        return sql.selectList("userRecipe.findByUserId", userId);
    }

    // 좋아요한 레시피 목록 조회
    public List<UserRecipeDTO> findLikedRecipes(Long userId) {
        return sql.selectList("userRecipe.findLikedRecipes", userId);
    }

    // 레시피 수정
    public void update(UserRecipeDTO recipeDTO) {
        sql.update("userRecipe.update", recipeDTO);
    }

    // 레시피 삭제
    public void deleteById(Long id) {
        sql.delete("userRecipe.deleteById", id);
    }

    // 조회수 증가
    public void incrementViewCount(Long id) {
        sql.update("userRecipe.incrementViewCount", id);
    }

    // 좋아요 수 업데이트 (캐시)
    public void updateLikeCount(Long recipeId) {
        sql.update("userRecipe.updateLikeCount", recipeId);
    }

    // 댓글 수 업데이트 (캐시)
    public void updateCommentCount(Long recipeId) {
        sql.update("userRecipe.updateCommentCount", recipeId);
    }

    // 전체 레시피 수 조회 (페이징용)
    public int countAll(String search) {
        return sql.selectOne("userRecipe.countAll", search);
    }
}
