package com.example.demo.TEST_001.scheduler;

import com.example.demo.TEST_001.dto.NotificationDTO;
import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.service.EmailService;
import com.example.demo.TEST_001.service.NotificationService;
import com.example.demo.TEST_001.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiryNotificationScheduler {
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserService userService;

    // 사용자 정보 캐시 (스케줄러 실행 중에만 사용)
    private final Map<Long, UserDTO> userCache = new HashMap<>();

    /**
     * 매일 오전 9시에 유통기한 임박 알림 생성
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiringIngredients() {
        log.info("=== 유통기한 임박 알림 스케줄러 시작 ===");
        long startTime = System.currentTimeMillis();

        try {
            // 캐시 초기화
            userCache.clear();

            // 유통기한 임박 식재료 조회 (D-3, D-1, D-Day)
            List<Map<String, Object>> expiringItems = notificationService.getExpiringIngredients();

            int createdCount = 0;
            int skippedCount = 0;
            int emailSentCount = 0;

            for (Map<String, Object> item : expiringItems) {
                Long userId = ((Number) item.get("userId")).longValue();
                Integer ingredientId = ((Number) item.get("ingredientId")).intValue();
                String ingredientName = (String) item.get("ingredientName");
                Integer daysUntilExpiry = ((Number) item.get("daysUntilExpiry")).intValue();

                // 알림 타입 및 메시지 결정
                String type;
                String title;
                String message;

                switch (daysUntilExpiry) {
                    case 3:
                        type = "EXPIRY_D3";
                        title = "유통기한 임박 알림";
                        message = String.format("[%s] 유통기한이 3일 남았습니다.", ingredientName);
                        break;
                    case 1:
                        type = "EXPIRY_D1";
                        title = "유통기한 임박 알림";
                        message = String.format("[%s] 내일이 유통기한입니다!", ingredientName);
                        break;
                    case 0:
                        type = "EXPIRY_DDAY";
                        title = "유통기한 D-Day!";
                        message = String.format("[%s] 오늘이 유통기한입니다! 빨리 사용하세요.", ingredientName);
                        break;
                    default:
                        continue;
                }

                // 알림 생성 (중복 체크는 Service에서 처리)
                NotificationDTO notification = notificationService.createNotification(
                        userId, ingredientId, type, title, message);

                if (notification != null) {
                    createdCount++;

                    // 이메일 발송 (비동기)
                    try {
                        UserDTO user = getCachedUser(userId);
                        if (user != null) {
                            emailService.sendExpiryNotificationEmail(user, notification);
                            emailSentCount++;
                        }
                    } catch (Exception e) {
                        log.warn("이메일 발송 요청 실패: userId={}", userId, e);
                    }
                } else {
                    skippedCount++;
                }
            }

            // 캐시 정리
            userCache.clear();

            long endTime = System.currentTimeMillis();
            log.info("=== 유통기한 알림 스케줄러 완료 ===");
            log.info("처리 시간: {}ms, 생성: {}건, 스킵(중복): {}건, 이메일요청: {}건",
                    (endTime - startTime), createdCount, skippedCount, emailSentCount);

        } catch (Exception e) {
            log.error("유통기한 알림 스케줄러 오류", e);
        }
    }

    /**
     * 사용자 정보 캐시 조회
     */
    private UserDTO getCachedUser(Long userId) {
        return userCache.computeIfAbsent(userId, id -> {
            try {
                return userService.getUserById(id);
            } catch (Exception e) {
                log.warn("사용자 조회 실패: userId={}", id, e);
                return null;
            }
        });
    }

    /**
     * 매일 새벽 3시에 오래된 알림 정리
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldNotifications() {
        log.info("=== 오래된 알림 정리 스케줄러 시작 ===");

        try {
            int deletedCount = notificationService.cleanupOldNotifications();
            log.info("삭제된 알림 수: {}건", deletedCount);
        } catch (Exception e) {
            log.error("알림 정리 스케줄러 오류", e);
        }
    }

    /**
     * 테스트용: 수동으로 스케줄러 실행
     * (개발 중 테스트 목적)
     */
    public void triggerManually() {
        log.info("수동 트리거 실행");
        checkExpiringIngredients();
    }
}
