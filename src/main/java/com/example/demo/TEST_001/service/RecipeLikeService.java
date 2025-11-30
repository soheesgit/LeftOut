package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.RecipeLikeDTO;
import com.example.demo.TEST_001.repository.RecipeLikeRepository;
import com.example.demo.TEST_001.repository.UserRecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeLikeService {
    private final RecipeLikeRepository recipeLikeRepository;
    private final UserRecipeRepository userRecipeRepository;

    // 좋아요 토글 (추가/삭제)
    @Transactional
    public boolean toggleLike(Long recipeId, Long userId) {
        boolean isLiked = recipeLikeRepository.existsByRecipeIdAndUserId(recipeId, userId);

        if (isLiked) {
            // 이미 좋아요한 경우 -> 좋아요 취소
            recipeLikeRepository.deleteByRecipeIdAndUserId(recipeId, userId);
        } else {
            // 좋아요하지 않은 경우 -> 좋아요 추가
            RecipeLikeDTO likeDTO = new RecipeLikeDTO();
            likeDTO.setRecipeId(recipeId);
            likeDTO.setUserId(userId);
            recipeLikeRepository.save(likeDTO);
        }

        // 레시피의 좋아요 수 업데이트
        userRecipeRepository.updateLikeCount(recipeId);

        return !isLiked; // 현재 좋아요 상태 반환
    }

    // 좋아요 여부 확인
    public boolean isLiked(Long recipeId, Long userId) {
        return recipeLikeRepository.existsByRecipeIdAndUserId(recipeId, userId);
    }

    // 레시피별 좋아요 수 조회
    public int getLikeCount(Long recipeId) {
        return recipeLikeRepository.countByRecipeId(recipeId);
    }
}
