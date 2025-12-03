package com.example.demo.TEST_001.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDTO {
    private String label;              // 영문 라벨 (예: "Meat")
    private Double score;              // 신뢰도 (0.0 ~ 1.0)
    private String koreanLabel;        // 한글 라벨 (예: "고기")
    private Integer suggestedCategoryId; // 추천 카테고리 ID (1:채소, 2:육류, 3:유제품, 4:과일, 5:조미료, 6:기타)
}
