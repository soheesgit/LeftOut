package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.RecipeCommentDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecipeCommentRepository {
    private final SqlSessionTemplate sql;

    // 댓글 생성
    public void save(RecipeCommentDTO commentDTO) {
        sql.insert("recipeComment.save", commentDTO);
    }

    // 댓글 조회 (ID로)
    public RecipeCommentDTO findById(Long id) {
        return sql.selectOne("recipeComment.findById", id);
    }

    // 레시피별 댓글 목록 조회
    public List<RecipeCommentDTO> findByRecipeId(Long recipeId) {
        return sql.selectList("recipeComment.findByRecipeId", recipeId);
    }

    // 댓글 수정
    public void update(RecipeCommentDTO commentDTO) {
        sql.update("recipeComment.update", commentDTO);
    }

    // 댓글 삭제
    public void deleteById(Long id) {
        sql.delete("recipeComment.deleteById", id);
    }

    // 레시피별 댓글 수 조회
    public int countByRecipeId(Long recipeId) {
        Integer count = sql.selectOne("recipeComment.countByRecipeId", recipeId);
        return count != null ? count : 0;
    }
}
