package com.example.demo.TEST_001.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuickAddRequestDTO {
    private String ingredientName;
    private Integer categoryId;
    private Double quantity;
    private String unit;
    private String storageLocation;  // 보관 위치 (냉장, 냉동, 실온)
}
