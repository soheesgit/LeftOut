package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.IngredientDefaultExpiryDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class IngredientDefaultExpiryRepository {
    private final SqlSessionTemplate sql;

    // 식재료명으로 기본 유통기한 조회
    public IngredientDefaultExpiryDTO getByName(String name) {
        return sql.selectOne("ingredientDefaultExpiry.getByName", name);
    }

    // 모든 기본 유통기한 정보 조회
    public List<IngredientDefaultExpiryDTO> getAll() {
        return sql.selectList("ingredientDefaultExpiry.getAll");
    }

    // 기본 유통기한 정보 추가
    public void save(IngredientDefaultExpiryDTO dto) {
        sql.insert("ingredientDefaultExpiry.save", dto);
    }
}
