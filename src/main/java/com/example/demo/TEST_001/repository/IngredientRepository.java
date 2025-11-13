package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.IngredientDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class IngredientRepository {
    private final SqlSessionTemplate sql;

    // 식재료 목록 조회 (유통기한 임박순, active 상태만)
    public List<IngredientDTO> getList(Long userId) {
        return sql.selectList("ingredient.getList", userId);
    }

    // 카테고리별 식재료 목록 조회
    public List<IngredientDTO> getListByCategory(Long userId, Integer categoryId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("categoryId", categoryId);
        return sql.selectList("ingredient.getListByCategory", params);
    }

    // 검색 및 필터링 식재료 목록 조회
    public List<IngredientDTO> getListWithFilter(Long userId, Integer categoryId, String searchKeyword, String storageLocation) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("categoryId", categoryId);
        params.put("searchKeyword", searchKeyword);
        params.put("storageLocation", storageLocation);
        return sql.selectList("ingredient.getListWithFilter", params);
    }

    // 식재료 상세 조회
    public IngredientDTO detail(Long userId, Integer id) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("id", id);
        return sql.selectOne("ingredient.detail", params);
    }

    // 식재료 추가
    public void save(IngredientDTO ingredientDTO) {
        sql.insert("ingredient.save", ingredientDTO);
    }

    // 식재료 수정
    public void update(IngredientDTO ingredientDTO) {
        sql.update("ingredient.update", ingredientDTO);
    }

    // 식재료 소비 완료 처리
    public void markAsConsumed(Long userId, Integer id) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("id", id);
        sql.update("ingredient.markAsConsumed", params);
    }

    // 식재료 완전 삭제
    public void delete(Long userId, Integer id) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("id", id);
        sql.delete("ingredient.delete", params);
    }

    // 카테고리별 식재료 개수 조회 (카테고리 삭제 검증용)
    public int countByCategoryId(Integer categoryId) {
        return sql.selectOne("ingredient.countByCategoryId", categoryId);
    }

    // 식재료 폐기 처리
    public void markAsDiscarded(Long userId, Integer id) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("id", id);
        sql.update("ingredient.markAsDiscarded", params);
    }

    // 통계: 기간별 폐기 통계 (일/주/월)
    public List<Map<String, Object>> getDiscardStatsByPeriod(Long userId, String period, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("period", period);
        params.put("limit", limit);
        return sql.selectList("ingredient.getDiscardStatsByPeriod", params);
    }

    // 통계: 카테고리별 폐기 통계
    public List<Map<String, Object>> getDiscardStatsByCategory(Long userId, String period, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("period", period);
        params.put("limit", limit);
        return sql.selectList("ingredient.getDiscardStatsByCategory", params);
    }

    // 통계: 월별 소비/폐기 통계
    public Map<String, Object> getMonthlyConsumptionStats(Long userId, int year, int month) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("year", year);
        params.put("month", month);
        return sql.selectOne("ingredient.getMonthlyConsumptionStats", params);
    }

    // 통계: 보관 위치별 카테고리 분포
    public List<Map<String, Object>> getStorageLocationDistribution(Long userId) {
        return sql.selectList("ingredient.getStorageLocationDistribution", userId);
    }

    // 통계: 현재 카테고리 구성 (active 식재료만)
    public List<Map<String, Object>> getCategoryComposition(Long userId) {
        return sql.selectList("ingredient.getCategoryComposition", userId);
    }
}
