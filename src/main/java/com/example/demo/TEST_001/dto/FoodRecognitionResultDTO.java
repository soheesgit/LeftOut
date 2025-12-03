package com.example.demo.TEST_001.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class FoodRecognitionResultDTO {
    private boolean success;
    private String message;
    private String originalFilename;
    private String imageUrl;
    private List<PredictionDTO> predictions;  // AI 예측 결과 목록
    private String topLabel;                  // 최상위 예측 라벨 (영문)
    private Double topScore;                  // 최상위 신뢰도
    private String koreanLabel;               // 최상위 예측 라벨 (한글)
    private Integer suggestedCategoryId;      // 추천 카테고리 ID
}
