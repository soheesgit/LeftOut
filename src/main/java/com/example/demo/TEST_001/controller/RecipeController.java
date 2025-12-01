package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.dto.UserRecipeDTO;
import com.example.demo.TEST_001.service.RecipeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    /**
     * 레시피 목록 페이지
     */
    @GetMapping("/list")
    public String recipeList(
            @RequestParam(required = false) String rcpWay2,  // 조리방법 (예: 찌기, 끓이기 등)
            @RequestParam(required = false) String rcpPat2,  // 요리종류 (예: 반찬, 국, 후식 등)
            @RequestParam(required = false) String searchRecipeName,  // 레시피명 검색어
            @RequestParam(required = false) String searchIngredient,  // 재료명 검색어
            @RequestParam(defaultValue = "1") int page,      // 페이지 번호
            HttpSession session,
            Model model) {

        // 로그인 체크
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 페이징 설정 (한 페이지당 20개)
        int pageSize = 20;

        // 레시피 목록 + 전체 개수 한번에 조회 (성능 최적화)
        Map<String, Object> result = recipeService.getRecipeListWithCount(
                loginUser.getId(),
                rcpWay2,
                rcpPat2,
                searchRecipeName,
                searchIngredient,
                page,
                pageSize
        );

        @SuppressWarnings("unchecked")
        List<UserRecipeDTO> recipes = (List<UserRecipeDTO>) result.get("recipes");
        int totalCount = (int) result.get("totalCount");
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 모델에 데이터 추가
        model.addAttribute("recipes", recipes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("rcpWay2", rcpWay2);
        model.addAttribute("rcpPat2", rcpPat2);
        model.addAttribute("searchRecipeName", searchRecipeName);
        model.addAttribute("searchIngredient", searchIngredient);

        return "recipeList";
    }

    /**
     * 레시피 상세 페이지
     */
    @GetMapping("/detail/{rcpSeq}")
    public String recipeDetail(
            @PathVariable String rcpSeq,
            HttpSession session,
            Model model) {

        // 로그인 체크
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 레시피 상세 정보 조회 (DB 기반)
        UserRecipeDTO recipe = recipeService.getRecipeDetail(rcpSeq, loginUser.getId());

        if (recipe == null) {
            model.addAttribute("errorMessage", "레시피를 찾을 수 없습니다.");
            return "redirect:/recipe/list";
        }

        model.addAttribute("recipe", recipe);

        return "recipeDetail";
    }
}
