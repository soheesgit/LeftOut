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

    // API 레시피 통합용 필드
    private String source;                   // 'user' 또는 'api'
    private String rcpSeq;                   // API 레시피 고유번호
    private String rcpWay2;                  // 조리방법
    private String rcpPat2;                  // 요리종류
    private String rcpPartsDtls;             // 재료정보 (API용)
    private String infoWgt;                  // 중량
    private String infoEng;                  // 열량
    private String infoCar;                  // 탄수화물
    private String infoPro;                  // 단백질
    private String infoFat;                  // 지방
    private String infoNa;                   // 나트륨
    private String rcpNaTip;                 // 저감 조리법 TIP
    private String hashTag;                  // 해시태그
    private String attFileNoMain;            // 이미지경로(소)
    private String attFileNoMk;              // 이미지경로(대)

    // 조리 단계 (API용)
    private String manual01, manual02, manual03, manual04, manual05;
    private String manual06, manual07, manual08, manual09, manual10;
    private String manual11, manual12, manual13, manual14, manual15;
    private String manual16, manual17, manual18, manual19, manual20;

    // 조리 단계 이미지 (API용)
    private String manualImg01, manualImg02, manualImg03, manualImg04, manualImg05;
    private String manualImg06, manualImg07, manualImg08, manualImg09, manualImg10;
    private String manualImg11, manualImg12, manualImg13, manualImg14, manualImg15;
    private String manualImg16, manualImg17, manualImg18, manualImg19, manualImg20;

    // 성능 최적화용 (미리 파싱된 재료 - DB 저장)
    private String parsedIngredients;        // JSON 배열: ["소금", "설탕", ...]
    private Integer ingredientCount;         // 재료 개수

    // 매칭 관련 필드 (계산용 - DB 미저장)
    private int matchedIngredientCount;
    private int totalIngredientCount;
    private double matchScore;
    private String matchedIngredients;

    // 추가 필드 (조인용)
    private String authorName;               // 작성자 이름
    private String authorUsername;           // 작성자 username
    private Boolean isLiked;                 // 현재 사용자가 좋아요 했는지 여부

    // 파싱된 데이터 (JSON -> 객체)
    private List<IngredientItemDTO> ingredientList;
    private List<RecipeStepDTO> stepList;
}
