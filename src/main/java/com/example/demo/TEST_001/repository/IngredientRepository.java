package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.IngredientDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class IngredientRepository {
    private final SqlSessionTemplate sql;

    // 식재료 목록 조회 (유통기한 임박순, active 상태만)
    public List<IngredientDTO> getList() {
        return sql.selectList("ingredient.getList");
    }

    // 카테고리별 식재료 목록 조회
    public List<IngredientDTO> getListByCategory(Integer categoryId) {
        return sql.selectList("ingredient.getListByCategory", categoryId);
    }

    // 식재료 상세 조회
    public IngredientDTO detail(Integer id) {
        return sql.selectOne("ingredient.detail", id);
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
    public void markAsConsumed(Integer id) {
        sql.update("ingredient.markAsConsumed", id);
    }

    // 식재료 완전 삭제
    public void delete(Integer id) {
        sql.delete("ingredient.delete", id);
    }
}
