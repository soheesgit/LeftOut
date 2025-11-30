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
public class IngredientItemDTO {
    private String name;            // 재료 이름
    private String amount;          // 양 (예: "200g", "2큰술")
}
