package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.CategoryDTO;
import com.example.demo.TEST_001.dto.IngredientDTO;
import com.example.demo.TEST_001.dto.IngredientDefaultExpiryDTO;
import com.example.demo.TEST_001.dto.QuickAddRequestDTO;
import com.example.demo.TEST_001.service.CategoryService;
import com.example.demo.TEST_001.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ingredient")
public class IngredientController {
    private final IngredientService ingredientService;
    private final CategoryService categoryService;

    // 식재료 목록 조회 (카테고리 필터 가능)
    @GetMapping("/list")
    public String getList(@RequestParam(required = false, defaultValue = "0") Integer categoryId, Model model) {
        List<IngredientDTO> ingredientList = ingredientService.getListByCategory(categoryId);
        List<CategoryDTO> categories = categoryService.getAll();

        model.addAttribute("ingredientList", ingredientList);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);

        return "ingredientList";
    }

    // 식재료 추가 폼
    @GetMapping("/add")
    public String addForm(Model model) {
        List<CategoryDTO> categories = categoryService.getAll();
        List<IngredientDefaultExpiryDTO> defaultExpiryList = ingredientService.getAllDefaultExpiry();

        model.addAttribute("categories", categories);
        model.addAttribute("defaultExpiryList", defaultExpiryList);

        return "addIngredient";
    }

    // 식재료 추가 처리
    @PostMapping("/add")
    public String save(@ModelAttribute IngredientDTO ingredientDTO) {
        ingredientService.save(ingredientDTO);
        return "redirect:/ingredient/list";
    }

    // 식재료 상세 조회
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        IngredientDTO ingredientDTO = ingredientService.detail(id);
        model.addAttribute("ingredient", ingredientDTO);
        return "detailIngredient";
    }

    // 식재료 수정 폼
    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Integer id, Model model) {
        IngredientDTO ingredientDTO = ingredientService.detail(id);
        List<CategoryDTO> categories = categoryService.getAll();

        model.addAttribute("ingredient", ingredientDTO);
        model.addAttribute("categories", categories);

        return "updateIngredient";
    }

    // 식재료 수정 처리
    @PostMapping("/update/{id}")
    public String update(@ModelAttribute IngredientDTO ingredientDTO) {
        ingredientService.update(ingredientDTO);
        return "redirect:/ingredient/detail/" + ingredientDTO.getIngredientId();
    }

    // 식재료 '다 먹음' 처리
    @GetMapping("/consume/{id}")
    public String consume(@PathVariable Integer id) {
        ingredientService.markAsConsumed(id);
        return "redirect:/ingredient/list";
    }

    // 식재료 완전 삭제
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        ingredientService.delete(id);
        return "redirect:/ingredient/list";
    }

    // AJAX: 식재료명으로 기본 유통기한 조회
    @GetMapping("/default-expiry/{name}")
    @ResponseBody
    public IngredientDefaultExpiryDTO getDefaultExpiry(@PathVariable String name) {
        return ingredientService.getDefaultExpiry(name);
    }

    // 빠른 추가 페이지
    @GetMapping("/quick-add")
    public String quickAddPage(Model model) {
        List<CategoryDTO> categories = categoryService.getAll();
        List<IngredientDefaultExpiryDTO> defaultExpiryList = ingredientService.getAllDefaultExpiry();

        model.addAttribute("categories", categories);
        model.addAttribute("defaultExpiryList", defaultExpiryList);

        return "quickAddIngredient";
    }

    // AJAX: 빠른 추가 처리
    @PostMapping("/quick-add")
    @ResponseBody
    public Map<String, Object> quickAdd(@RequestBody QuickAddRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // IngredientDTO 생성
            IngredientDTO ingredientDTO = new IngredientDTO();
            ingredientDTO.setIngredientName(request.getIngredientName());
            ingredientDTO.setCategoryId(request.getCategoryId());
            ingredientDTO.setQuantity(request.getQuantity());
            ingredientDTO.setPurchaseDate(LocalDate.now()); // 오늘 날짜

            // 유통기한은 null로 두면 Service에서 자동 계산됨
            ingredientDTO.setExpiryDate(null);

            // 저장
            ingredientService.save(ingredientDTO);

            response.put("success", true);
            response.put("message", request.getIngredientName() + "이(가) 추가되었습니다!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "추가 중 오류가 발생했습니다: " + e.getMessage());
        }

        return response;
    }
}
