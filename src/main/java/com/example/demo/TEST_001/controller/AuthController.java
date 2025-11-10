package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    // 회원가입 폼
    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(UserDTO userDTO, Model model) {
        try {
            userService.signup(userDTO);
            return "redirect:/login?signup=success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    // 로그인 폼
    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String signup,
                           @RequestParam(required = false) String error,
                           Model model) {
        if ("success".equals(signup)) {
            model.addAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
        }
        if (error != null) {
            model.addAttribute("error", "로그인이 필요합니다.");
        }
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        try {
            UserDTO user = userService.login(username, password);
            // 세션에 사용자 정보 저장
            session.setAttribute("loginUser", user);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
