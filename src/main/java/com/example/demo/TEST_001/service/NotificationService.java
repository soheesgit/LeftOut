package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.NotificationDTO;
import com.example.demo.TEST_001.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // 사용자별 SSE 연결 관리 (userId -> List<SseEmitter>)
    // 한 사용자가 여러 탭/브라우저에서 접속할 수 있으므로 List 사용
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // SSE 타임아웃: 30분 (밀리초)
    private static final Long SSE_TIMEOUT = 30 * 60 * 1000L;

    /**
     * SSE 연결 생성
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 사용자별 emitter 리스트에 추가
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 연결 완료/타임아웃/에러 시 제거
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        // 초기 연결 시 이벤트 전송
        try {
            // 연결 확인 이벤트
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));

            // 안읽은 알림 개수 전송
            int unreadCount = notificationRepository.countUnread(userId);
            emitter.send(SseEmitter.event()
                    .name("unread-count")
                    .data(unreadCount));

        } catch (IOException e) {
            log.error("SSE 초기 이벤트 전송 실패: userId={}", userId, e);
            removeEmitter(userId, emitter);
        }

        log.info("SSE 연결 생성: userId={}, 현재 연결 수={}",
                userId, emitters.get(userId).size());

        return emitter;
    }

    /**
     * Emitter 제거
     */
    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
        log.debug("SSE 연결 종료: userId={}", userId);
    }

    /**
     * 특정 사용자에게 알림 전송
     */
    public void sendNotification(Long userId, NotificationDTO notification) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            log.debug("SSE 연결 없음: userId={}", userId);
            return;
        }

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            } catch (IOException e) {
                log.warn("SSE 알림 전송 실패: userId={}", userId);
                removeEmitter(userId, emitter);
            }
        }
    }

    /**
     * 안읽은 알림 개수 업데이트 전송
     */
    public void sendUnreadCount(Long userId) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        int unreadCount = notificationRepository.countUnread(userId);

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("unread-count")
                        .data(unreadCount));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    /**
     * 알림 생성 및 저장 (+ SSE 전송)
     */
    @Transactional
    public NotificationDTO createNotification(Long userId, Integer ingredientId,
                                              String type, String title, String message) {
        // 중복 체크
        if (ingredientId != null &&
                notificationRepository.existsTodayNotification(userId, ingredientId, type)) {
            log.debug("중복 알림 스킵: userId={}, ingredientId={}, type={}",
                    userId, ingredientId, type);
            return null;
        }

        NotificationDTO notification = NotificationDTO.builder()
                .userId(userId)
                .ingredientId(ingredientId)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // SSE로 실시간 전송
        sendNotification(userId, notification);
        sendUnreadCount(userId);

        return notification;
    }

    /**
     * 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotifications(Long userId) {
        return notificationRepository.getListByUserId(userId);
    }

    /**
     * 안읽은 알림 개수
     */
    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnread(userId);
    }

    /**
     * 단일 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        notificationRepository.markAsRead(userId, notificationId);
        sendUnreadCount(userId);
    }

    /**
     * 전체 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
        sendUnreadCount(userId);
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        notificationRepository.delete(userId, notificationId);
        sendUnreadCount(userId);
    }

    /**
     * 유통기한 임박 식재료 조회 (스케줄러용)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getExpiringIngredients() {
        return notificationRepository.getExpiringIngredients();
    }

    /**
     * 오래된 알림 정리 (스케줄러용)
     */
    @Transactional
    public int cleanupOldNotifications() {
        return notificationRepository.deleteOldNotifications();
    }

    /**
     * 연결된 사용자 확인
     */
    public boolean isUserConnected(Long userId) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
        return userEmitters != null && !userEmitters.isEmpty();
    }
}
