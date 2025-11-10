package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {
    private final SqlSessionTemplate sql;

    // 모든 카테고리 조회
    public List<CategoryDTO> getAll() {
        return sql.selectList("category.getAll");
    }

    // 카테고리 상세 조회
    public CategoryDTO getById(Integer id) {
        return sql.selectOne("category.getById", id);
    }

    // 카테고리 추가
    public void save(CategoryDTO categoryDTO) {
        sql.insert("category.save", categoryDTO);
    }

    // 카테고리 삭제
    public void delete(Integer id) {
        sql.delete("category.delete", id);
    }
}
