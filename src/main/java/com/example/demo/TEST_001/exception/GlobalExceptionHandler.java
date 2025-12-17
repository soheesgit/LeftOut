package com.example.demo.TEST_001.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 * 모든 Controller에서 발생하는 예외를 잡아서 처리
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * AsyncRequestNotUsableException 처리 (SSE 연결 종료 시)
     * 클라이언트가 연결을 끊었을 때 발생하는 정상적인 상황이므로 무시
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        // 에러 로그 출력 안 함 (정상적인 클라이언트 연결 종료)
        log.debug("클라이언트가 비동기 요청을 종료했습니다: {}", e.getMessage());
    }

    /**
     * ClientAbortException 처리 (클라이언트 연결 중단)
     * 클라이언트가 응답을 받기 전에 연결을 끊었을 때 발생
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException e) {
        // 에러 로그 출력 안 함 (정상적인 클라이언트 연결 종료)
        log.debug("클라이언트가 연결을 중단했습니다: {}", e.getMessage());
    }

    /**
     * IOException 처리 (네트워크 연결 문제)
     * 클라이언트 연결 종료로 인한 IOException은 무시
     */
    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException e) {
        String message = e.getMessage();

        // 클라이언트 연결 종료 관련 메시지는 무시
        if (message != null && (
            message.contains("현재 연결은") ||
            message.contains("사용자의 호스트") ||
            message.contains("중단되었습니다") ||
            message.contains("Connection reset") ||
            message.contains("Broken pipe") ||
            message.contains("Connection aborted") ||
            message.contains("연결이 끊어졌습니다") ||
            message.contains("Socket closed")
        )) {
            log.debug("네트워크 연결 종료 (무시): {}", message);
            return;
        }

        // 실제 IO 에러는 로그 출력
        log.warn("IO 예외 발생: {}", message);
    }

    /**
     * 일반 페이지 요청에서 발생한 예외 처리
     * Whitelabel Error Page 대신 사용자 친화적인 에러 페이지 표시
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request, Model model) {
        // AsyncRequestNotUsableException과 ClientAbortException은 이미 별도 핸들러에서 처리하지만
        // 혹시 모를 경우를 대비해 여기서도 한 번 더 체크
        if (e instanceof AsyncRequestNotUsableException ||
            e.getCause() instanceof AsyncRequestNotUsableException) {
            log.debug("비동기 요청 사용 불가 (무시): {}", e.getMessage());
            return null;
        }
        if (e instanceof ClientAbortException ||
            e.getCause() instanceof ClientAbortException) {
            log.debug("클라이언트 연결 중단 (무시): {}", e.getMessage());
            return null;
        }

        // IOException 체크 (네트워크 연결 종료)
        if (e instanceof IOException || e.getCause() instanceof IOException) {
            String message = e.getMessage();
            if (message != null && (
                message.contains("현재 연결은") ||
                message.contains("사용자의 호스트") ||
                message.contains("중단되었습니다") ||
                message.contains("Connection reset") ||
                message.contains("Broken pipe") ||
                message.contains("Connection aborted")
            )) {
                log.debug("네트워크 연결 종료 (무시): {}", message);
                return null;
            }
        }

        log.error("예외 발생: {} - {}", request.getRequestURI(), e.getMessage(), e);

        // AJAX 요청인지 확인
        String ajaxHeader = request.getHeader("X-Requested-With");
        String acceptHeader = request.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader) ||
                (acceptHeader != null && acceptHeader.contains("application/json"));

        if (isAjax) {
            // AJAX 요청이면 JSON 응답
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", getUserFriendlyMessage(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // 일반 요청이면 에러 페이지로 이동
        model.addAttribute("errorMessage", getUserFriendlyMessage(e));
        model.addAttribute("errorDetail", e.getMessage());
        return "error";
    }

    /**
     * IllegalArgumentException 처리 (입력 검증 실패)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request, Model model) {
        log.warn("입력 검증 실패: {} - {}", request.getRequestURI(), e.getMessage());

        String ajaxHeader = request.getHeader("X-Requested-With");
        String acceptHeader = request.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader) ||
                (acceptHeader != null && acceptHeader.contains("application/json"));

        if (isAjax) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }

    /**
     * SecurityException 처리 (권한 없음)
     */
    @ExceptionHandler(SecurityException.class)
    public Object handleSecurityException(SecurityException e, HttpServletRequest request, Model model) {
        log.warn("권한 없음: {} - {}", request.getRequestURI(), e.getMessage());

        String ajaxHeader = request.getHeader("X-Requested-With");
        String acceptHeader = request.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader) ||
                (acceptHeader != null && acceptHeader.contains("application/json"));

        if (isAjax) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "권한이 없습니다.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        model.addAttribute("errorMessage", "권한이 없습니다.");
        return "error";
    }

    /**
     * 사용자 친화적인 에러 메시지 생성
     */
    private String getUserFriendlyMessage(Exception e) {
        String message = e.getMessage();

        if (message == null || message.isEmpty()) {
            return "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }

        // 기술적인 메시지를 사용자 친화적으로 변환
        if (message.contains("Connection refused") || message.contains("connect timed out")) {
            return "서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.";
        }
        if (message.contains("NullPointer")) {
            return "데이터를 찾을 수 없습니다.";
        }
        if (message.contains("DataIntegrity")) {
            return "데이터 처리 중 오류가 발생했습니다.";
        }

        return message;
    }
}
