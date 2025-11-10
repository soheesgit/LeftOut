package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.CategoryDTO;
import com.example.demo.TEST_001.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;

    // 카테고리 관리 페이지
    @GetMapping("/manage")
    public String manage(Model model) {
        List<CategoryDTO> categories = categoryService.getAll();
        model.addAttribute("categories", categories);
        return "categoryManagement";
    }

    // 카테고리 추가
    @PostMapping("/add")
    public String add(@ModelAttribute CategoryDTO categoryDTO) {
        categoryService.save(categoryDTO);
        return "redirect:/category/manage";
    }

    // 카테고리 삭제
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return "redirect:/category/manage";
    }
}
