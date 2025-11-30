package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.RecipeCommentDTO;
import com.example.demo.TEST_001.repository.RecipeCommentRepository;
import com.example.demo.TEST_001.repository.UserRecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeCommentService {
    private final RecipeCommentRepository recipeCommentRepository;
    private final UserRecipeRepository userRecipeRepository;

    // 댓글 작성
    @Transactional
    public RecipeCommentDTO createComment(RecipeCommentDTO commentDTO) {
        recipeCommentRepository.save(commentDTO);

        // 레시피의 댓글 수 업데이트
        userRecipeRepository.updateCommentCount(commentDTO.getRecipeId());

        return commentDTO;
    }

    // 레시피별 댓글 목록 조회
    public List<RecipeCommentDTO> getCommentsByRecipeId(Long recipeId) {
        return recipeCommentRepository.findByRecipeId(recipeId);
    }

    // 댓글 수정
    @Transactional
    public void updateComment(Long commentId, String content, Long currentUserId) {
        // 기존 댓글 조회
        RecipeCommentDTO existingComment = recipeCommentRepository.findById(commentId);
        if (existingComment == null) {
            throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
        }

        // 권한 확인 (작성자만 수정 가능)
        if (!existingComment.getUserId().equals(currentUserId)) {
            throw new SecurityException("댓글을 수정할 권한이 없습니다.");
        }

        existingComment.setContent(content);
        recipeCommentRepository.update(existingComment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        // 기존 댓글 조회
        RecipeCommentDTO existingComment = recipeCommentRepository.findById(commentId);
        if (existingComment == null) {
            throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
        }

        // 권한 확인 (작성자만 삭제 가능)
        if (!existingComment.getUserId().equals(currentUserId)) {
            throw new SecurityException("댓글을 삭제할 권한이 없습니다.");
        }

        Long recipeId = existingComment.getRecipeId();
        recipeCommentRepository.deleteById(commentId);

        // 레시피의 댓글 수 업데이트
        userRecipeRepository.updateCommentCount(recipeId);
    }
}
