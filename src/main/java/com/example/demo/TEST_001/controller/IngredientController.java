package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.CategoryDTO;
import com.example.demo.TEST_001.dto.IngredientDTO;
import com.example.demo.TEST_001.dto.IngredientDefaultExpiryDTO;
import com.example.demo.TEST_001.dto.QuickAddRequestDTO;
import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.service.CategoryService;
import com.example.demo.TEST_001.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
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

    // 식재료 목록 조회 (카테고리 필터 및 검색 가능)
    @GetMapping("/list")
    public String getList(@RequestParam(required = false, defaultValue = "0") Integer categoryId,
                          @RequestParam(required = false) String searchKeyword,
                          Model model, HttpSession session) {
        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<IngredientDTO> ingredientList = ingredientService.getListWithFilter(
            loginUser.getId(), categoryId, searchKeyword);
        List<CategoryDTO> categories = categoryService.getAll();

        model.addAttribute("ingredientList", ingredientList);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("searchKeyword", searchKeyword != null ? searchKeyword : "");

        return "ingredientList";
    }

    // 식재료 추가 폼
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<CategoryDTO> categories = categoryService.getAll();
        List<IngredientDefaultExpiryDTO> defaultExpiryList = ingredientService.getAllDefaultExpiry();

        model.addAttribute("categories", categories);
        model.addAttribute("defaultExpiryList", defaultExpiryList);

        return "addIngredient";
    }

    // 식재료 추가 처리
    @PostMapping("/add")
    public String save(@ModelAttribute IngredientDTO ingredientDTO, HttpSession session) {
        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        ingredientService.save(loginUser.getId(), ingredientDTO);
        return "redirect:/ingredient/list";
    }

    // 식재료 상세 조회
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model, HttpSession session) {
        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        IngredientDTO ingredientDTO = ingredientService.detail(loginUser.getId(), id);
        model.addAttribute("ingredient", ingredientDTO);
        return "detailIngredient";
    }

    // 식재료 수정 폼
    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Integer id, Model model, HttpSession session) {
        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        IngredientDTO ingredientDTO = ingredientService.detail(loginUser.getId(), id);
        List<CategoryDTO> categories = categoryService.getAll();

        model.addAttribute("ingredient", ingredientDTO);
        model.addAttribute("categories", categories);

        return "updateIngredient";
    }

    // 식재료 수정 처리
    @PostMapping("/update/{id}")
    public String update(@ModelAttribute IngredientDTO ingredientDTO, HttpSession session) {
        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        ingredientService.update(loginUser.getId(), ingredientDTO);
        return "redirect:/ingredient/detail/" + ingredientDTO.getIngredientId();
    }

    // 식재료 '다 먹음' 처리
    @GetMapping("/consume/{id}")
    public String consume(@PathVariable Integer id, HttpSession session) {
        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        ingredientService.markAsConsumed(loginUser.getId(), id);
        return "redirect:/ingredient/list";
    }

    // 식재료 완전 삭제
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        ingredientService.delete(loginUser.getId(), id);
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
    public String quickAddPage(Model model, HttpSession session) {
        // 로그인 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<CategoryDTO> categories = categoryService.getAll();
        List<IngredientDefaultExpiryDTO> defaultExpiryList = ingredientService.getAllDefaultExpiry();

        model.addAttribute("categories", categories);
        model.addAttribute("defaultExpiryList", defaultExpiryList);

        return "quickAddIngredient";
    }

    // AJAX: 빠른 추가 처리
    @PostMapping("/quick-add")
    @ResponseBody
    public Map<String, Object> quickAdd(@RequestBody QuickAddRequestDTO request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 로그인 확인
            UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
            if (loginUser == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            // 입력 검증
            if (request == null || request.getIngredientName() == null || request.getIngredientName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "식재료 이름을 입력해주세요.");
                return response;
            }

            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                response.put("success", false);
                response.put("message", "수량을 올바르게 입력해주세요.");
                return response;
            }

            if (request.getUnit() == null || request.getUnit().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "단위를 선택해주세요.");
                return response;
            }

            // IngredientDTO 생성
            IngredientDTO ingredientDTO = new IngredientDTO();
            ingredientDTO.setIngredientName(request.getIngredientName());
            ingredientDTO.setCategoryId(request.getCategoryId());
            ingredientDTO.setQuantity(request.getQuantity());
            ingredientDTO.setUnit(request.getUnit());
            ingredientDTO.setPurchaseDate(LocalDate.now()); // 오늘 날짜

            // 유통기한은 null로 두면 Service에서 자동 계산됨
            ingredientDTO.setExpiryDate(null);

            // 저장
            ingredientService.save(loginUser.getId(), ingredientDTO);

            response.put("success", true);
            response.put("message", request.getIngredientName() + "이(가) 추가되었습니다!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "추가 중 오류가 발생했습니다: " + e.getMessage());
        }

        return response;
    }
}
