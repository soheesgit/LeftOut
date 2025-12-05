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

    // ========================================
    // API 레시피 통합용 메서드
    // ========================================

    // API 레시피 저장
    public void saveApiRecipe(UserRecipeDTO recipeDTO) {
        sql.insert("userRecipe.saveApiRecipe", recipeDTO);
    }

    // API 레시피 중복 체크
    public boolean existsByRcpSeq(String rcpSeq) {
        return sql.selectOne("userRecipe.existsByRcpSeq", rcpSeq);
    }

    // API 레시피 개수 조회
    public int countApiRecipes() {
        return sql.selectOne("userRecipe.countApiRecipes");
    }

    // API 레시피 전체 조회 (필터링/검색 포함)
    public List<UserRecipeDTO> findApiRecipes(String rcpWay2, String rcpPat2,
                                               String searchRecipeName, String searchIngredient) {
        Map<String, Object> params = new HashMap<>();
        params.put("rcpWay2", rcpWay2);
        params.put("rcpPat2", rcpPat2);
        params.put("searchRecipeName", searchRecipeName);
        params.put("searchIngredient", searchIngredient);
        return sql.selectList("userRecipe.findApiRecipes", params);
    }

    // API 레시피 조회 (사용자 식재료 필터링 포함) - 성능 최적화
    public List<UserRecipeDTO> findApiRecipesWithIngredients(String rcpWay2, String rcpPat2,
                                                              String searchRecipeName, String searchIngredient,
                                                              List<String> ingredientNames) {
        Map<String, Object> params = new HashMap<>();
        params.put("rcpWay2", rcpWay2);
        params.put("rcpPat2", rcpPat2);
        params.put("searchRecipeName", searchRecipeName);
        params.put("searchIngredient", searchIngredient);
        params.put("ingredientNames", ingredientNames);
        return sql.selectList("userRecipe.findApiRecipesWithIngredients", params);
    }

    // API 레시피 상세 조회 (rcp_seq로)
    public UserRecipeDTO findByRcpSeq(String rcpSeq) {
        return sql.selectOne("userRecipe.findByRcpSeq", rcpSeq);
    }

    // ========================================
    // 랜덤 레시피 추천용 메서드
    // ========================================

    // API 레시피에서 랜덤 추천
    public List<UserRecipeDTO> findRandomApiRecipe(int count) {
        return sql.selectList("userRecipe.findRandomApiRecipe", count);
    }

    // 사용자 레시피에서 랜덤 추천
    public List<UserRecipeDTO> findRandomUserRecipe(int count) {
        return sql.selectList("userRecipe.findRandomUserRecipe", count);
    }

    // 전체 레시피에서 랜덤 추천
    public List<UserRecipeDTO> findRandomRecipe(int count) {
        return sql.selectList("userRecipe.findRandomRecipe", count);
    }

    // 전체 레시피 수 조회
    public int countTotalRecipes() {
        Integer count = sql.selectOne("userRecipe.countTotalRecipes");
        return count != null ? count : 0;
    }

    // 식재료 매칭 퍼센트 기반 가중치 랜덤 추천
    public List<UserRecipeDTO> findWeightedRandomApiRecipe(Long userId, int count) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("count", count);
        return sql.selectList("userRecipe.findWeightedRandomApiRecipe", params);
    }

    // ========================================
    // 통합 레시피 상세 조회
    // ========================================

    // 통합 레시피 상세 조회 (ID 기반, 좋아요 여부 포함)
    public UserRecipeDTO findByIdIntegrated(Long id, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("userId", userId);
        return sql.selectOne("userRecipe.findByIdIntegrated", params);
    }
}
