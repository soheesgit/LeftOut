package com.example.demo.TEST_001.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class RecipeCommentDTO {
    private Long id;
    private Long recipeId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 추가 필드 (조인용)
    private String authorName;          // 댓글 작성자 이름
    private String authorUsername;      // 댓글 작성자 username
}
