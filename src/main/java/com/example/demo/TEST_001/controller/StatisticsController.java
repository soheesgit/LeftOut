package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.service.StatisticsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    /**
     * 통계 페이지 (메인)
     */
    @GetMapping
    public String statisticsPage(Model model, HttpSession session) {
        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 기본 통계 데이터 (일 단위, 최근 7일)
        Map<String, Object> dashboardStats = statisticsService.getDashboardStats(
            loginUser.getId(), "day", 7
        );

        model.addAttribute("stats", dashboardStats);
        model.addAttribute("selectedPeriod", "day");

        return "statistics";
    }

    /**
     * AJAX: 기간별 통계 데이터 조회
     * @param period 기간 단위 (day, week, month)
     * @return JSON 통계 데이터
     */
    @GetMapping("/data")
    @ResponseBody
    public Map<String, Object> getStatisticsData(
            @RequestParam(defaultValue = "day") String period,
            HttpSession session) {

        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        // 기간에 따른 limit 설정
        int limit;
        switch (period) {
            case "week":
                limit = 12; // 최근 12주
                break;
            case "month":
                limit = 12; // 최근 12개월
                break;
            case "day":
            default:
                limit = 7; // 최근 7일
                break;
        }

        return statisticsService.getDashboardStats(loginUser.getId(), period, limit);
    }

    /**
     * AJAX: 기간별 폐기 통계만 조회 (차트 업데이트용)
     */
    @GetMapping("/discard-by-period")
    @ResponseBody
    public Map<String, Object> getDiscardByPeriod(
            @RequestParam(defaultValue = "day") String period,
            HttpSession session) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        int limit = switch (period) {
            case "week" -> 12;
            case "month" -> 12;
            default -> 7;
        };

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("discardByPeriod", statisticsService.getDiscardStatsByPeriod(loginUser.getId(), period, limit));
        result.put("discardByCategory", statisticsService.getDiscardStatsByCategory(loginUser.getId(), period, limit));

        return result;
    }
}
