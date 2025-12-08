package com.example.demo.TEST_001.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long notificationId;
    private Long userId;
    private Integer ingredientId;
    private String type;              // EXPIRY_D3, EXPIRY_D1, EXPIRY_DDAY, SYSTEM
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // JOIN용 필드
    private String ingredientName;
}
