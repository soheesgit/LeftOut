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
    public List<IngredientDTO> getList(Long userId) {
        return ingredientRepository.getList(userId);
    }

    // 카테고리별 식재료 목록 조회
    public List<IngredientDTO> getListByCategory(Long userId, Integer categoryId) {
        if (categoryId == null || categoryId == 0) {
            return getList(userId);
        }
        return ingredientRepository.getListByCategory(userId, categoryId);
    }

    // 검색 및 필터링 식재료 목록 조회
    public List<IngredientDTO> getListWithFilter(Long userId, Integer categoryId, String searchKeyword, String storageLocation) {
        // 검색어, 카테고리, 보관 위치 모두 없으면 전체 목록
        if ((searchKeyword == null || searchKeyword.trim().isEmpty()) &&
            (categoryId == null || categoryId == 0) &&
            (storageLocation == null || storageLocation.trim().isEmpty())) {
            return getList(userId);
        }
        return ingredientRepository.getListWithFilter(userId, categoryId, searchKeyword, storageLocation);
    }

    // 식재료 상세 조회
    public IngredientDTO detail(Long userId, Integer id) {
        return ingredientRepository.detail(userId, id);
    }

    // 식재료 추가
    public void save(Long userId, IngredientDTO ingredientDTO) {
        // 입력 검증
        if (ingredientDTO == null) {
            throw new IllegalArgumentException("식재료 정보가 없습니다.");
        }
        if (ingredientDTO.getIngredientName() == null || ingredientDTO.getIngredientName().trim().isEmpty()) {
            throw new IllegalArgumentException("식재료 이름은 필수입니다.");
        }

        // 수량 검증 (선택사항이므로 null은 허용하되, 값이 있으면 0보다 커야 함)
        if (ingredientDTO.getQuantity() != null && ingredientDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }

        // 단위 검증 (수량이 있으면 단위도 필수)
        if (ingredientDTO.getQuantity() != null && (ingredientDTO.getUnit() == null || ingredientDTO.getUnit().trim().isEmpty())) {
            throw new IllegalArgumentException("수량을 입력한 경우 단위도 선택해야 합니다.");
        }

        // userId 설정
        ingredientDTO.setUserId(userId);

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
    public void update(Long userId, IngredientDTO ingredientDTO) {
        // userId 설정 (보안 검증을 위해)
        ingredientDTO.setUserId(userId);
        ingredientRepository.update(ingredientDTO);
    }

    // 식재료 '다 먹음' 처리 (소비 완료)
    public void markAsConsumed(Long userId, Integer id) {
        ingredientRepository.markAsConsumed(userId, id);
    }

    // 식재료 '폐기' 처리
    public void markAsDiscarded(Long userId, Integer id) {
        ingredientRepository.markAsDiscarded(userId, id);
    }

    // 식재료 완전 삭제
    public void delete(Long userId, Integer id) {
        ingredientRepository.delete(userId, id);
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
