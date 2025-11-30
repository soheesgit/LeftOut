package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.UserRecipeDTO;
import com.example.demo.TEST_001.dto.IngredientItemDTO;
import com.example.demo.TEST_001.dto.RecipeStepDTO;
import com.example.demo.TEST_001.repository.UserRecipeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRecipeService {
    private final UserRecipeRepository userRecipeRepository;
    private final FileUploadService fileUploadService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 레시피 생성
    @Transactional
    public UserRecipeDTO createRecipe(UserRecipeDTO recipeDTO, MultipartFile mainImage) throws IOException {
        // 대표 이미지 업로드
        if (mainImage != null && !mainImage.isEmpty()) {
            String imagePath = fileUploadService.uploadFile(mainImage);
            recipeDTO.setMainImagePath(imagePath);
        }

        // 재료 리스트 JSON 변환
        if (recipeDTO.getIngredientList() != null) {
            String ingredientsJson = objectMapper.writeValueAsString(recipeDTO.getIngredientList());
            recipeDTO.setIngredients(ingredientsJson);
        }

        // 조리 단계 JSON 변환
        if (recipeDTO.getStepList() != null) {
            String stepsJson = objectMapper.writeValueAsString(recipeDTO.getStepList());
            recipeDTO.setCookingSteps(stepsJson);
        }

        userRecipeRepository.save(recipeDTO);
        return recipeDTO;
    }

    // 레시피 조회 (조회수 증가 + JSON 파싱)
    @Transactional
    public UserRecipeDTO getRecipe(Long id, Long currentUserId) {
        UserRecipeDTO recipe;

        if (currentUserId != null) {
            recipe = userRecipeRepository.findByIdWithLikeStatus(id, currentUserId);
        } else {
            recipe = userRecipeRepository.findById(id);
        }

        if (recipe == null) {
            throw new IllegalArgumentException("존재하지 않는 레시피입니다.");
        }

        // 조회수 증가
        userRecipeRepository.incrementViewCount(id);

        // JSON 파싱
        parseJsonFields(recipe);

        return recipe;
    }

    // 레시피 조회 (조회수 증가 없이)
    public UserRecipeDTO getRecipeWithoutViewIncrement(Long id) {
        UserRecipeDTO recipe = userRecipeRepository.findById(id);
        if (recipe == null) {
            throw new IllegalArgumentException("존재하지 않는 레시피입니다.");
        }
        parseJsonFields(recipe);
        return recipe;
    }

    // 전체 레시피 목록 조회
    public List<UserRecipeDTO> getAllRecipes(String search, Integer page, Integer size) {
        Integer offset = null;
        Integer limit = null;

        if (page != null && size != null) {
            offset = page * size;
            limit = size;
        }

        return userRecipeRepository.findAll(search, limit, offset);
    }

    // 사용자별 레시피 목록 조회
    public List<UserRecipeDTO> getMyRecipes(Long userId) {
        return userRecipeRepository.findByUserId(userId);
    }

    // 좋아요한 레시피 목록 조회
    public List<UserRecipeDTO> getLikedRecipes(Long userId) {
        return userRecipeRepository.findLikedRecipes(userId);
    }

    // 레시피 수정
    @Transactional
    public void updateRecipe(Long id, UserRecipeDTO recipeDTO, Long currentUserId, MultipartFile newMainImage) throws IOException {
        // 기존 레시피 조회
        UserRecipeDTO existingRecipe = userRecipeRepository.findById(id);
        if (existingRecipe == null) {
            throw new IllegalArgumentException("존재하지 않는 레시피입니다.");
        }

        // 권한 확인 (작성자만 수정 가능)
        if (!existingRecipe.getUserId().equals(currentUserId)) {
            throw new SecurityException("레시피를 수정할 권한이 없습니다.");
        }

        // 새 이미지가 있으면 기존 이미지 삭제 후 새 이미지 업로드
        if (newMainImage != null && !newMainImage.isEmpty()) {
            if (existingRecipe.getMainImagePath() != null) {
                fileUploadService.deleteFile(existingRecipe.getMainImagePath());
            }
            String imagePath = fileUploadService.uploadFile(newMainImage);
            recipeDTO.setMainImagePath(imagePath);
        }

        // 재료 리스트 JSON 변환
        if (recipeDTO.getIngredientList() != null) {
            String ingredientsJson = objectMapper.writeValueAsString(recipeDTO.getIngredientList());
            recipeDTO.setIngredients(ingredientsJson);
        }

        // 조리 단계 JSON 변환
        if (recipeDTO.getStepList() != null) {
            String stepsJson = objectMapper.writeValueAsString(recipeDTO.getStepList());
            recipeDTO.setCookingSteps(stepsJson);
        }

        recipeDTO.setId(id);
        userRecipeRepository.update(recipeDTO);
    }

    // 레시피 삭제
    @Transactional
    public void deleteRecipe(Long id, Long currentUserId) {
        // 기존 레시피 조회
        UserRecipeDTO existingRecipe = userRecipeRepository.findById(id);
        if (existingRecipe == null) {
            throw new IllegalArgumentException("존재하지 않는 레시피입니다.");
        }

        // 권한 확인 (작성자만 삭제 가능)
        if (!existingRecipe.getUserId().equals(currentUserId)) {
            throw new SecurityException("레시피를 삭제할 권한이 없습니다.");
        }

        // 이미지 파일 삭제
        if (existingRecipe.getMainImagePath() != null) {
            fileUploadService.deleteFile(existingRecipe.getMainImagePath());
        }

        userRecipeRepository.deleteById(id);
    }

    // 전체 레시피 수 조회
    public int getTotalCount(String search) {
        return userRecipeRepository.countAll(search);
    }

    // JSON 필드 파싱
    private void parseJsonFields(UserRecipeDTO recipe) {
        try {
            // 재료 리스트 파싱
            if (recipe.getIngredients() != null) {
                List<IngredientItemDTO> ingredientList = objectMapper.readValue(
                        recipe.getIngredients(),
                        new TypeReference<List<IngredientItemDTO>>() {}
                );
                recipe.setIngredientList(ingredientList);
            }

            // 조리 단계 파싱
            if (recipe.getCookingSteps() != null) {
                List<RecipeStepDTO> stepList = objectMapper.readValue(
                        recipe.getCookingSteps(),
                        new TypeReference<List<RecipeStepDTO>>() {}
                );
                recipe.setStepList(stepList);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 오류", e);
        }
    }
}
