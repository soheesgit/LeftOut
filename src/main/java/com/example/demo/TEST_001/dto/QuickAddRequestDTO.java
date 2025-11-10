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
    private String quantity;
}
