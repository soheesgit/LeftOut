package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.RecipeCommentDTO;
import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.dto.UserRecipeDTO;
import com.example.demo.TEST_001.service.RecipeCommentService;
import com.example.demo.TEST_001.service.RecipeLikeService;
import com.example.demo.TEST_001.service.RecipeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeLikeService recipeLikeService;
    private final RecipeCommentService recipeCommentService;

    /**
     * 통합 레시피 목록 페이지 (API + 사용자)
     */
    @GetMapping("/list")
    public String recipeList(
            @RequestParam(required = false, defaultValue = "all") String source,  // all, api, user
            @RequestParam(required = false) String rcpWay2,
            @RequestParam(required = false) String rcpPat2,
            @RequestParam(required = false) String searchRecipeName,
            @RequestParam(required = false) String searchIngredient,
            @RequestParam(required = false) String searchAuthor,
            @RequestParam(defaultValue = "1") int page,
            HttpSession session,
            Model model) {

        // 로그인 체크
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 페이징 설정
        int pageSize = 20;

        // 통합 레시피 목록 조회
        Map<String, Object> result = recipeService.getIntegratedRecipeListWithCount(
                loginUser.getId(),
                source,
                rcpWay2,
                rcpPat2,
                searchRecipeName,
                searchIngredient,
                searchAuthor,
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
        model.addAttribute("source", source);
        model.addAttribute("rcpWay2", rcpWay2);
        model.addAttribute("rcpPat2", rcpPat2);
        model.addAttribute("searchRecipeName", searchRecipeName);
        model.addAttribute("searchIngredient", searchIngredient);
        model.addAttribute("searchAuthor", searchAuthor);

        return "recipeList";
    }

    /**
     * 통합 레시피 상세 페이지 (ID 기반)
     */
    @GetMapping("/detail/{id}")
    public String recipeDetail(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        // 로그인 체크
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 통합 레시피 상세 조회
        UserRecipeDTO recipe = recipeService.getIntegratedRecipeDetail(id, loginUser.getId());

        if (recipe == null) {
            model.addAttribute("errorMessage", "레시피를 찾을 수 없습니다.");
            return "redirect:/recipe/list";
        }

        // 댓글 목록 조회
        List<RecipeCommentDTO> comments = recipeCommentService.getCommentsByRecipeId(id);

        model.addAttribute("recipe", recipe);
        model.addAttribute("comments", comments);
        model.addAttribute("loginUser", loginUser);

        return "recipeDetail";
    }

    /**
     * 기존 URL 호환성: rcpSeq로 접근 시 id로 리다이렉트
     */
    @GetMapping("/detail/seq/{rcpSeq}")
    public String recipeDetailBySeq(
            @PathVariable String rcpSeq,
            HttpSession session,
            Model model) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // rcpSeq로 레시피 조회하여 id 찾기
        UserRecipeDTO recipe = recipeService.getRecipeDetail(rcpSeq, loginUser.getId());
        if (recipe == null) {
            return "redirect:/recipe/list";
        }

        return "redirect:/recipe/detail/" + recipe.getId();
    }

    // ========================================
    // 좋아요/댓글 기능 (통합)
    // ========================================

    /**
     * 좋아요 토글 (AJAX)
     */
    @PostMapping("/{recipeId}/like")
    @ResponseBody
    public Map<String, Object> toggleLike(
            @PathVariable Long recipeId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        try {
            boolean isLiked = recipeLikeService.toggleLike(recipeId, loginUser.getId());
            int likeCount = recipeLikeService.getLikeCount(recipeId);

            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("likeCount", likeCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 처리 중 오류가 발생했습니다.");
        }

        return response;
    }

    /**
     * 댓글 작성
     */
    @PostMapping("/{recipeId}/comment")
    public String addComment(
            @PathVariable Long recipeId,
            @RequestParam String content,
            HttpSession session) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        RecipeCommentDTO comment = new RecipeCommentDTO();
        comment.setRecipeId(recipeId);
        comment.setUserId(loginUser.getId());
        comment.setContent(content);

        recipeCommentService.createComment(comment);

        return "redirect:/recipe/detail/" + recipeId;
    }

    /**
     * 댓글 수정
     */
    @PostMapping("/comment/{commentId}/edit")
    public String editComment(
            @PathVariable Long commentId,
            @RequestParam String content,
            @RequestParam Long recipeId,
            HttpSession session) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        recipeCommentService.updateComment(commentId, content, loginUser.getId());

        return "redirect:/recipe/detail/" + recipeId;
    }

    /**
     * 댓글 삭제
     */
    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long recipeId,
            HttpSession session) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        recipeCommentService.deleteComment(commentId, loginUser.getId());

        return "redirect:/recipe/detail/" + recipeId;
    }
}
