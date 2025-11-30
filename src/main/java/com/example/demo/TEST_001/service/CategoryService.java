package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.CategoryDTO;
import com.example.demo.TEST_001.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // 모든 카테고리 조회
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAll() {
        return categoryRepository.getAll();
    }

    // 카테고리 상세 조회
    @Transactional(readOnly = true)
    public CategoryDTO getById(Integer id) {
        return categoryRepository.getById(id);
    }

    // 카테고리 추가
    @Transactional
    public void save(CategoryDTO categoryDTO) {
        categoryRepository.save(categoryDTO);
    }

    // 카테고리 수정
    @Transactional
    public void update(CategoryDTO categoryDTO) {
        categoryRepository.update(categoryDTO);
    }
}
