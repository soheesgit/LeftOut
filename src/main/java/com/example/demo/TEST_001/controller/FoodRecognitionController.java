package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.CategoryDTO;
import com.example.demo.TEST_001.dto.FoodRecognitionResultDTO;
import com.example.demo.TEST_001.dto.IngredientDefaultExpiryDTO;
import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.service.CategoryService;
import com.example.demo.TEST_001.service.FoodRecognitionService;
import com.example.demo.TEST_001.service.IngredientService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ingredient")
public class FoodRecognitionController {

    private final FoodRecognitionService foodRecognitionService;
    private final CategoryService categoryService;
    private final IngredientService ingredientService;

    /**
     * AI 이미지 인식 페이지
     */
    @GetMapping("/ai-recognition")
    public String aiRecognitionPage(Model model, HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<CategoryDTO> categories = categoryService.getAll();
        List<IngredientDefaultExpiryDTO> defaultExpiryList = ingredientService.getAllDefaultExpiry();

        model.addAttribute("categories", categories);
        model.addAttribute("defaultExpiryList", defaultExpiryList);

        return "aiRecognition";
    }

    /**
     * 이미지 업로드 후 AI 인식 (AJAX)
     */
    @PostMapping("/ai-recognize")
    @ResponseBody
    public FoodRecognitionResultDTO recognizeFood(
            @RequestParam("image") MultipartFile image,
            HttpSession session) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            FoodRecognitionResultDTO result = new FoodRecognitionResultDTO();
            result.setSuccess(false);
            result.setMessage("로그인이 필요합니다.");
            return result;
        }

        return foodRecognitionService.recognizeFood(image);
    }

    /**
     * 인식 결과 기반 추천 식재료 목록 (AJAX)
     */
    @GetMapping("/ai-suggestions/{label}")
    @ResponseBody
    public Map<String, Object> getSuggestions(@PathVariable String label) {
        Map<String, Object> response = new HashMap<>();
        List<String> suggestions = foodRecognitionService.getSuggestedIngredients(label);
        response.put("suggestions", suggestions);
        return response;
    }

    /**
     * 지원하는 모든 AI 카테고리 목록 (AJAX)
     */
    @GetMapping("/ai-categories")
    @ResponseBody
    public Map<String, Object> getAllCategories() {
        Map<String, Object> response = new HashMap<>();
        response.put("categories", foodRecognitionService.getAllCategories());
        return response;
    }

    /**
     * AI 인식 결과로 식재료 바로 추가 (AJAX)
     */
    @PostMapping("/ai-add")
    @ResponseBody
    public Map<String, Object> addFromAI(
            @RequestParam String ingredientName,
            @RequestParam(required = false, defaultValue = "6") Integer categoryId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        try {
            // IngredientDTO 생성
            com.example.demo.TEST_001.dto.IngredientDTO dto =
                    new com.example.demo.TEST_001.dto.IngredientDTO();
            dto.setIngredientName(ingredientName);
            dto.setCategoryId(categoryId);
            dto.setPurchaseDate(java.time.LocalDate.now());
            dto.setStorageLocation("냉장");

            // 기존 IngredientService.save() 사용 (유통기한 자동 계산됨)
            ingredientService.save(loginUser.getId(), dto);

            response.put("success", true);
            response.put("message", ingredientName + "이(가) 냉장고에 추가되었습니다!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "추가 실패: " + e.getMessage());
        }

        return response;
    }
}
