package com.example.demo.TEST_001.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class UserRecipeDTO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String ingredients;              // JSON 문자열
    private String cookingSteps;             // JSON 문자열
    private Integer preparationTime;         // 준비 시간 (분)
    private Integer cookingTime;             // 조리 시간 (분)
    private Integer servings;                // 몇 인분
    private String difficultyLevel;          // 난이도: easy, medium, hard
    private String mainImagePath;            // 대표 이미지 경로
    private Integer viewCount;               // 조회수
    private Integer likeCount;               // 좋아요 수
    private Integer commentCount;            // 댓글 수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 추가 필드 (조인용)
    private String authorName;               // 작성자 이름
    private String authorUsername;           // 작성자 username
    private Boolean isLiked;                 // 현재 사용자가 좋아요 했는지 여부

    // 파싱된 데이터 (JSON -> 객체)
    private List<IngredientItemDTO> ingredientList;
    private List<RecipeStepDTO> stepList;
}
