package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {
    private final SqlSessionTemplate sql;

    // 알림 저장
    public void save(NotificationDTO notification) {
        sql.insert("notification.save", notification);
    }

    // 사용자별 알림 목록 조회 (최근 30일, 최신순)
    public List<NotificationDTO> getListByUserId(Long userId) {
        return sql.selectList("notification.getListByUserId", userId);
    }

    // 안읽은 알림 개수
    public int countUnread(Long userId) {
        Integer count = sql.selectOne("notification.countUnread", userId);
        return count != null ? count : 0;
    }

    // 안읽은 알림 목록
    public List<NotificationDTO> getUnreadByUserId(Long userId) {
        return sql.selectList("notification.getUnreadByUserId", userId);
    }

    // 단일 알림 읽음 처리
    public void markAsRead(Long userId, Long notificationId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("notificationId", notificationId);
        sql.update("notification.markAsRead", params);
    }

    // 전체 읽음 처리
    public void markAllAsRead(Long userId) {
        sql.update("notification.markAllAsRead", userId);
    }

    // 알림 삭제
    public void delete(Long userId, Long notificationId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("notificationId", notificationId);
        sql.delete("notification.delete", params);
    }

    // 오래된 알림 정리 (30일 이상)
    public int deleteOldNotifications() {
        return sql.delete("notification.deleteOldNotifications");
    }

    // 중복 알림 체크 (오늘 같은 타입/식재료 알림이 있는지)
    public boolean existsTodayNotification(Long userId, Integer ingredientId, String type) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("ingredientId", ingredientId);
        params.put("type", type);
        Integer count = sql.selectOne("notification.existsTodayNotification", params);
        return count != null && count > 0;
    }

    // 유통기한 임박 식재료 조회 (스케줄러용)
    public List<Map<String, Object>> getExpiringIngredients() {
        return sql.selectList("notification.getExpiringIngredients");
    }
}
