package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.IngredientDTO;
import com.example.demo.TEST_001.dto.IngredientDefaultExpiryDTO;
import com.example.demo.TEST_001.repository.IngredientDefaultExpiryRepository;
import com.example.demo.TEST_001.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final IngredientDefaultExpiryRepository defaultExpiryRepository;
    private final RecipeMatchService recipeMatchService;

    // 식재료 목록 조회 (유통기한 임박순)
    @Transactional(readOnly = true)
    public List<IngredientDTO> getList(Long userId) {
        return ingredientRepository.getList(userId);
    }

    // 카테고리별 식재료 목록 조회
    @Transactional(readOnly = true)
    public List<IngredientDTO> getListByCategory(Long userId, Integer categoryId) {
        if (categoryId == null || categoryId == 0) {
            return getList(userId);
        }
        return ingredientRepository.getListByCategory(userId, categoryId);
    }

    // 검색 및 필터링 식재료 목록 조회
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public IngredientDTO detail(Long userId, Integer id) {
        return ingredientRepository.detail(userId, id);
    }

    // 식재료 추가
    @Transactional
    public void save(Long userId, IngredientDTO ingredientDTO) {
        // 입력 검증
        if (ingredientDTO == null) {
            throw new IllegalArgumentException("식재료 정보가 없습니다.");
        }
        if (ingredientDTO.getIngredientName() == null || ingredientDTO.getIngredientName().trim().isEmpty()) {
            throw new IllegalArgumentException("식재료 이름은 필수입니다.");
        }

        // 수량이 0 또는 음수인 경우 null로 처리 (수량 미입력으로 간주)
        if (ingredientDTO.getQuantity() != null && ingredientDTO.getQuantity() <= 0) {
            ingredientDTO.setQuantity(null);
            ingredientDTO.setUnit(null);  // 수량이 없으면 단위도 불필요
        }

        // 단위 검증 (수량이 있으면 단위도 필수)
        if (ingredientDTO.getQuantity() != null && ingredientDTO.getQuantity() > 0
                && (ingredientDTO.getUnit() == null || ingredientDTO.getUnit().trim().isEmpty())) {
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

        // 카테고리 필수 검증 (자동 설정 후에도 없으면 오류)
        if (ingredientDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리를 선택해주세요.");
        }

        // 날짜 유효성 검증 (유통기한은 구매일보다 이전일 수 없음)
        if (ingredientDTO.getExpiryDate() != null && ingredientDTO.getPurchaseDate() != null) {
            if (ingredientDTO.getExpiryDate().isBefore(ingredientDTO.getPurchaseDate())) {
                throw new IllegalArgumentException("유통기한은 구매일보다 이전일 수 없습니다.");
            }
        }

        ingredientRepository.save(ingredientDTO);

        // 매칭 점수 비동기 재계산
        recipeMatchService.recalculateMatchScoresAsync(userId);
    }

    // 식재료 수정
    @Transactional
    public void update(Long userId, IngredientDTO ingredientDTO) {
        // userId 설정 (보안 검증을 위해)
        ingredientDTO.setUserId(userId);
        ingredientRepository.update(ingredientDTO);
    }

    // 식재료 '다 먹음' 처리 (소비 완료)
    @Transactional
    public void markAsConsumed(Long userId, Integer id) {
        ingredientRepository.markAsConsumed(userId, id);
        // 매칭 점수 비동기 재계산
        recipeMatchService.recalculateMatchScoresAsync(userId);
    }

    // 식재료 '폐기' 처리
    @Transactional
    public void markAsDiscarded(Long userId, Integer id) {
        ingredientRepository.markAsDiscarded(userId, id);
        // 매칭 점수 비동기 재계산
        recipeMatchService.recalculateMatchScoresAsync(userId);
    }

    // 식재료 완전 삭제
    @Transactional
    public void delete(Long userId, Integer id) {
        ingredientRepository.delete(userId, id);
        // 매칭 점수 비동기 재계산
        recipeMatchService.recalculateMatchScoresAsync(userId);
    }

    // 식재료명으로 기본 유통기한 조회
    @Transactional(readOnly = true)
    public IngredientDefaultExpiryDTO getDefaultExpiry(String ingredientName) {
        return defaultExpiryRepository.getByName(ingredientName);
    }

    // 모든 기본 유통기한 정보 조회
    @Transactional(readOnly = true)
    public List<IngredientDefaultExpiryDTO> getAllDefaultExpiry() {
        return defaultExpiryRepository.getAll();
    }
}
