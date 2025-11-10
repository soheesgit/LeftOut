package com.example.demo.TEST_001.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class IngredientDTO {
    private Integer ingredientId;
    private String ingredientName;
    private Integer categoryId;
    private String categoryName;  // JOIN을 위한 필드
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private LocalDate consumeDate;
    private String quantity;
    private String memo;
    private String status;  // active, consumed
    private Integer daysUntilExpiry;  // 유통기한까지 남은 일수 (계산용)
}
