package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.CategoryDTO;
import com.example.demo.TEST_001.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // 모든 카테고리 조회
    public List<CategoryDTO> getAll() {
        return categoryRepository.getAll();
    }

    // 카테고리 상세 조회
    public CategoryDTO getById(Integer id) {
        return categoryRepository.getById(id);
    }

    // 카테고리 추가
    public void save(CategoryDTO categoryDTO) {
        categoryRepository.save(categoryDTO);
    }

    // 카테고리 삭제
    public void delete(Integer id) {
        categoryRepository.delete(id);
    }
}
