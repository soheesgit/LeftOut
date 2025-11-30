package com.example.demo.TEST_001.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class RecipeLikeDTO {
    private Long id;
    private Long recipeId;
    private Long userId;
    private LocalDateTime createdAt;

    // 추가 필드 (조인용)
    private String recipeTitle;         // 레시피 제목
    private String authorName;          // 레시피 작성자 이름
}
