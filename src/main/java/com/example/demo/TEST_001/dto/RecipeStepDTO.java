package com.example.demo.TEST_001.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStepDTO {
    private Integer step;           // 단계 번호
    private String description;     // 단계 설명
    private String imagePath;       // 단계별 이미지 경로
}
