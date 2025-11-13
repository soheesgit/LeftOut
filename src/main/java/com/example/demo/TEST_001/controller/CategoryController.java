package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.CategoryDTO;
import com.example.demo.TEST_001.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;

    // 카테고리 관리 페이지
    @GetMapping("/manage")
    public String manage(Model model, @RequestParam(required = false) String error, @RequestParam(required = false) String success) {
        List<CategoryDTO> categories = categoryService.getAll();
        model.addAttribute("categories", categories);

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        if (success != null) {
            model.addAttribute("successMessage", success);
        }

        return "categoryManagement";
    }

    // 카테고리 추가
    @PostMapping("/add")
    public String add(@ModelAttribute CategoryDTO categoryDTO) {
        categoryService.save(categoryDTO);
        return "redirect:/category/manage";
    }

    // 카테고리 수정
    @PostMapping("/update")
    public String update(@ModelAttribute CategoryDTO categoryDTO, RedirectAttributes redirectAttributes) {
        try {
            categoryService.update(categoryDTO);
            redirectAttributes.addAttribute("success", "카테고리가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "카테고리 수정에 실패했습니다: " + e.getMessage());
        }
        return "redirect:/category/manage";
    }
}
