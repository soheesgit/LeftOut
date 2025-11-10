package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.IngredientDTO;
import com.example.demo.TEST_001.dto.IngredientDefaultExpiryDTO;
import com.example.demo.TEST_001.repository.IngredientDefaultExpiryRepository;
import com.example.demo.TEST_001.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final IngredientDefaultExpiryRepository defaultExpiryRepository;

    // 식재료 목록 조회 (유통기한 임박순)
    public List<IngredientDTO> getList() {
        return ingredientRepository.getList();
    }

    // 카테고리별 식재료 목록 조회
    public List<IngredientDTO> getListByCategory(Integer categoryId) {
        if (categoryId == null || categoryId == 0) {
            return getList();
        }
        return ingredientRepository.getListByCategory(categoryId);
    }

    // 식재료 상세 조회
    public IngredientDTO detail(Integer id) {
        return ingredientRepository.detail(id);
    }

    // 식재료 추가
    public void save(IngredientDTO ingredientDTO) {
        // 유통기한이 null인 경우, 기본 유통기한 조회하여 자동 계산
        if (ingredientDTO.getExpiryDate() == null && ingredientDTO.getPurchaseDate() != null) {
            IngredientDefaultExpiryDTO defaultExpiry =
                defaultExpiryRepository.getByName(ingredientDTO.getIngredientName());

            if (defaultExpiry != null) {
                LocalDate expiryDate = ingredientDTO.getPurchaseDate()
                    .plusDays(defaultExpiry.getDefaultExpiryDays());
                ingredientDTO.setExpiryDate(expiryDate);

                // 카테고리가 없으면 기본 카테고리 설정
                if (ingredientDTO.getCategoryId() == null) {
                    ingredientDTO.setCategoryId(defaultExpiry.getCategoryId());
                }
            }
        }

        ingredientRepository.save(ingredientDTO);
    }

    // 식재료 수정
    public void update(IngredientDTO ingredientDTO) {
        ingredientRepository.update(ingredientDTO);
    }

    // 식재료 '다 먹음' 처리 (소비 완료)
    public void markAsConsumed(Integer id) {
        ingredientRepository.markAsConsumed(id);
    }

    // 식재료 완전 삭제
    public void delete(Integer id) {
        ingredientRepository.delete(id);
    }

    // 식재료명으로 기본 유통기한 조회
    public IngredientDefaultExpiryDTO getDefaultExpiry(String ingredientName) {
        return defaultExpiryRepository.getByName(ingredientName);
    }

    // 모든 기본 유통기한 정보 조회
    public List<IngredientDefaultExpiryDTO> getAllDefaultExpiry() {
        return defaultExpiryRepository.getAll();
    }
}
