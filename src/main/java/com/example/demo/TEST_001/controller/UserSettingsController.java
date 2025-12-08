package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.service.EmailService;
import com.example.demo.TEST_001.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/settings")
public class UserSettingsController {
    private final UserService userService;
    private final EmailService emailService;

    /**
     * 이메일 설정 페이지
     */
    @GetMapping("/email")
    public String emailSettingsPage(HttpSession session, Model model) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 최신 사용자 정보 조회
        UserDTO user = userService.getUserById(loginUser.getId());
        model.addAttribute("user", user);

        return "emailSettings";
    }

    /**
     * 이메일 설정 업데이트 API
     */
    @PostMapping("/email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateEmailSettings(
            @RequestParam(required = false) String email,
            @RequestParam(required = false, defaultValue = "false") Boolean emailNotificationEnabled,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 이메일 형식 검증
            if (email != null && !email.isBlank() && !isValidEmail(email)) {
                response.put("success", false);
                response.put("message", "올바른 이메일 형식이 아닙니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 빈 문자열은 null로 처리
            if (email != null && email.isBlank()) {
                email = null;
            }

            userService.updateEmailSettings(loginUser.getId(), email, emailNotificationEnabled);

            // 세션 정보 업데이트
            loginUser.setEmail(email);
            loginUser.setEmailNotificationEnabled(emailNotificationEnabled);
            session.setAttribute("loginUser", loginUser);

            response.put("success", true);
            response.put("message", "이메일 설정이 저장되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("이메일 설정 업데이트 오류", e);
            response.put("success", false);
            response.put("message", "설정 저장 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 테스트 이메일 발송
     */
    @PostMapping("/email/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendTestEmail(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        // 최신 사용자 정보 조회
        UserDTO user = userService.getUserById(loginUser.getId());

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            response.put("success", false);
            response.put("message", "이메일을 먼저 저장해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean sent = emailService.sendTestEmail(user.getEmail());

        if (sent) {
            response.put("success", true);
            response.put("message", "테스트 이메일이 발송되었습니다. 받은편지함을 확인해주세요.");
        } else {
            response.put("success", false);
            response.put("message", "이메일 발송에 실패했습니다. 서버 설정을 확인해주세요.");
        }

        return ResponseEntity.ok(response);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
