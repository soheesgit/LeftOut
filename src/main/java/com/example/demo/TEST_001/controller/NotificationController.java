package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.NotificationDTO;
import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * SSE 구독 엔드포인트
     * produces = MediaType.TEXT_EVENT_STREAM_VALUE 필수
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            // 로그인하지 않은 경우 빈 emitter 반환 후 즉시 완료
            SseEmitter emitter = new SseEmitter(0L);
            emitter.complete();
            return emitter;
        }

        return notificationService.subscribe(loginUser.getId());
    }

    /**
     * 알림 목록 조회 API
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNotifications(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        List<NotificationDTO> notifications = notificationService.getNotifications(loginUser.getId());
        int unreadCount = notificationService.getUnreadCount(loginUser.getId());

        response.put("success", true);
        response.put("notifications", notifications);
        response.put("unreadCount", unreadCount);

        return ResponseEntity.ok(response);
    }

    /**
     * 안읽은 알림 개수 조회
     */
    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }

        int count = notificationService.getUnreadCount(loginUser.getId());

        response.put("success", true);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * 단일 알림 읽음 처리
     */
    @PostMapping("/read/{notificationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long notificationId,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        notificationService.markAsRead(loginUser.getId(), notificationId);

        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 알림 읽음 처리
     */
    @PostMapping("/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        notificationService.markAllAsRead(loginUser.getId());

        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable Long notificationId,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        notificationService.deleteNotification(loginUser.getId(), notificationId);

        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
