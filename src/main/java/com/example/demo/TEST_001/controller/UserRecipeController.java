package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.IngredientItemDTO;
import com.example.demo.TEST_001.dto.RecipeCommentDTO;
import com.example.demo.TEST_001.dto.RecipeStepDTO;
import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.dto.UserRecipeDTO;
import com.example.demo.TEST_001.service.RecipeCommentService;
import com.example.demo.TEST_001.service.RecipeLikeService;
import com.example.demo.TEST_001.service.UserRecipeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class UserRecipeController {
    private final UserRecipeService userRecipeService;
    private final RecipeCommentService commentService;
    private final RecipeLikeService likeService;

    // ==================== 레시피 목록 ====================

    // 내 레시피 목록
    @GetMapping("/my-recipes")
    public String myRecipes(HttpSession session, Model model) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        List<UserRecipeDTO> recipes = userRecipeService.getMyRecipes(loginUser.getId());
        model.addAttribute("recipes", recipes);
        model.addAttribute("pageTitle", "내가 작성한 레시피");
        return "myRecipes";
    }

    // 전체 레시피 목록
    @GetMapping("/all-recipes")
    public String allRecipes(@RequestParam(required = false) String search,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size,
                             Model model) {
        List<UserRecipeDTO> recipes = userRecipeService.getAllRecipes(search, page, size);
        int totalCount = userRecipeService.getTotalCount(search);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        model.addAttribute("recipes", recipes);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageTitle", "모든 레시피");
        return "allRecipes";
    }

    // 좋아요한 레시피 목록
    @GetMapping("/liked-recipes")
    public String likedRecipes(HttpSession session, Model model) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        List<UserRecipeDTO> recipes = userRecipeService.getLikedRecipes(loginUser.getId());
        model.addAttribute("recipes", recipes);
        model.addAttribute("pageTitle", "좋아요한 레시피");
        return "myRecipes";
    }

    // ==================== 레시피 작성 ====================

    // 레시피 작성 폼
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("recipe", new UserRecipeDTO());
        return "createRecipe";
    }

    // 레시피 작성 처리
    @PostMapping("/create")
    public String create(@RequestParam String title,
                        @RequestParam(required = false) String description,
                        @RequestParam("ingredientNames[]") List<String> ingredientNames,
                        @RequestParam("ingredientAmounts[]") List<String> ingredientAmounts,
                        @RequestParam("stepDescriptions[]") List<String> stepDescriptions,
                        @RequestParam(required = false) Integer preparationTime,
                        @RequestParam(required = false) Integer cookingTime,
                        @RequestParam(required = false) Integer servings,
                        @RequestParam(required = false) String difficultyLevel,
                        @RequestParam(required = false) MultipartFile mainImage,
                        HttpSession session,
                        Model model) {
        try {
            UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
            if (loginUser == null) {
                return "redirect:/login";
            }

            // UserRecipeDTO 생성
            UserRecipeDTO recipeDTO = new UserRecipeDTO();
            recipeDTO.setUserId(loginUser.getId());
            recipeDTO.setTitle(title);
            recipeDTO.setDescription(description);
            recipeDTO.setPreparationTime(preparationTime);
            recipeDTO.setCookingTime(cookingTime);
            recipeDTO.setServings(servings);
            recipeDTO.setDifficultyLevel(difficultyLevel);

            // 재료 리스트 생성
            List<IngredientItemDTO> ingredientList = new ArrayList<>();
            for (int i = 0; i < ingredientNames.size(); i++) {
                if (ingredientNames.get(i) != null && !ingredientNames.get(i).trim().isEmpty()) {
                    ingredientList.add(new IngredientItemDTO(
                            ingredientNames.get(i),
                            ingredientAmounts.get(i)
                    ));
                }
            }
            recipeDTO.setIngredientList(ingredientList);

            // 조리 단계 리스트 생성
            List<RecipeStepDTO> stepList = new ArrayList<>();
            for (int i = 0; i < stepDescriptions.size(); i++) {
                if (stepDescriptions.get(i) != null && !stepDescriptions.get(i).trim().isEmpty()) {
                    stepList.add(new RecipeStepDTO(
                            i + 1,
                            stepDescriptions.get(i),
                            null
                    ));
                }
            }
            recipeDTO.setStepList(stepList);

            // 레시피 저장
            UserRecipeDTO savedRecipe = userRecipeService.createRecipe(recipeDTO, mainImage);

            return "redirect:/recipe/user-recipe/" + savedRecipe.getId();
        } catch (Exception e) {
            model.addAttribute("error", "레시피 등록 중 오류가 발생했습니다: " + e.getMessage());
            return "createRecipe";
        }
    }

    // ==================== 레시피 상세보기 ====================

    @GetMapping("/user-recipe/{id}")
    public String recipeDetail(@PathVariable Long id, HttpSession session, Model model) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        Long currentUserId = loginUser != null ? loginUser.getId() : null;

        // 레시피 조회
        UserRecipeDTO recipe = userRecipeService.getRecipe(id, currentUserId);

        // 댓글 조회
        List<RecipeCommentDTO> comments = commentService.getCommentsByRecipeId(id);

        model.addAttribute("recipe", recipe);
        model.addAttribute("comments", comments);
        model.addAttribute("loginUser", loginUser);

        return "userRecipeDetail";
    }

    // ==================== 레시피 수정 ====================

    // 레시피 수정 폼
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        UserRecipeDTO recipe = userRecipeService.getRecipeWithoutViewIncrement(id);

        // 권한 확인
        if (!recipe.getUserId().equals(loginUser.getId())) {
            return "redirect:/recipe/user-recipe/" + id + "?error=unauthorized";
        }

        model.addAttribute("recipe", recipe);
        return "editRecipe";
    }

    // 레시피 수정 처리
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                      @RequestParam String title,
                      @RequestParam(required = false) String description,
                      @RequestParam("ingredientNames[]") List<String> ingredientNames,
                      @RequestParam("ingredientAmounts[]") List<String> ingredientAmounts,
                      @RequestParam("stepDescriptions[]") List<String> stepDescriptions,
                      @RequestParam(required = false) Integer preparationTime,
                      @RequestParam(required = false) Integer cookingTime,
                      @RequestParam(required = false) Integer servings,
                      @RequestParam(required = false) String difficultyLevel,
                      @RequestParam(required = false) MultipartFile mainImage,
                      HttpSession session,
                      Model model) {
        try {
            UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
            if (loginUser == null) {
                return "redirect:/login";
            }

            // UserRecipeDTO 생성
            UserRecipeDTO recipeDTO = new UserRecipeDTO();
            recipeDTO.setTitle(title);
            recipeDTO.setDescription(description);
            recipeDTO.setPreparationTime(preparationTime);
            recipeDTO.setCookingTime(cookingTime);
            recipeDTO.setServings(servings);
            recipeDTO.setDifficultyLevel(difficultyLevel);

            // 재료 리스트 생성
            List<IngredientItemDTO> ingredientList = new ArrayList<>();
            for (int i = 0; i < ingredientNames.size(); i++) {
                if (ingredientNames.get(i) != null && !ingredientNames.get(i).trim().isEmpty()) {
                    ingredientList.add(new IngredientItemDTO(
                            ingredientNames.get(i),
                            ingredientAmounts.get(i)
                    ));
                }
            }
            recipeDTO.setIngredientList(ingredientList);

            // 조리 단계 리스트 생성
            List<RecipeStepDTO> stepList = new ArrayList<>();
            for (int i = 0; i < stepDescriptions.size(); i++) {
                if (stepDescriptions.get(i) != null && !stepDescriptions.get(i).trim().isEmpty()) {
                    stepList.add(new RecipeStepDTO(
                            i + 1,
                            stepDescriptions.get(i),
                            null
                    ));
                }
            }
            recipeDTO.setStepList(stepList);

            // 레시피 수정
            userRecipeService.updateRecipe(id, recipeDTO, loginUser.getId(), mainImage);

            return "redirect:/recipe/user-recipe/" + id;
        } catch (SecurityException e) {
            return "redirect:/recipe/user-recipe/" + id + "?error=unauthorized";
        } catch (Exception e) {
            model.addAttribute("error", "레시피 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "editRecipe";
        }
    }

    // ==================== 레시피 삭제 ====================

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        try {
            UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
            if (loginUser == null) {
                return "redirect:/login";
            }
            userRecipeService.deleteRecipe(id, loginUser.getId());
            return "redirect:/recipe/my-recipes";
        } catch (SecurityException e) {
            return "redirect:/recipe/user-recipe/" + id + "?error=unauthorized";
        }
    }

    // ==================== 좋아요 ====================

    @PostMapping("/{recipeId}/like")
    @ResponseBody
    public Map<String, Object> toggleLike(@PathVariable Long recipeId, HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        Map<String, Object> response = new HashMap<>();

        if (loginUser == null) {
            response.put("error", "로그인이 필요합니다.");
            return response;
        }

        boolean isLiked = likeService.toggleLike(recipeId, loginUser.getId());
        int likeCount = likeService.getLikeCount(recipeId);

        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount);
        return response;
    }

    // ==================== 댓글 ====================

    // 댓글 작성
    @PostMapping("/{recipeId}/comment")
    public String addComment(@PathVariable Long recipeId,
                            @RequestParam String content,
                            HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        RecipeCommentDTO commentDTO = new RecipeCommentDTO();
        commentDTO.setRecipeId(recipeId);
        commentDTO.setUserId(loginUser.getId());
        commentDTO.setContent(content);

        commentService.createComment(commentDTO);

        return "redirect:/recipe/user-recipe/" + recipeId;
    }

    // 댓글 수정
    @PostMapping("/comment/{commentId}/edit")
    public String editComment(@PathVariable Long commentId,
                             @RequestParam String content,
                             @RequestParam Long recipeId,
                             HttpSession session) {
        try {
            UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
            if (loginUser == null) {
                return "redirect:/login";
            }
            commentService.updateComment(commentId, content, loginUser.getId());
            return "redirect:/recipe/user-recipe/" + recipeId;
        } catch (SecurityException e) {
            return "redirect:/recipe/user-recipe/" + recipeId + "?error=unauthorized";
        }
    }

    // 댓글 삭제
    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                               @RequestParam Long recipeId,
                               HttpSession session) {
        try {
            UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
            if (loginUser == null) {
                return "redirect:/login";
            }
            commentService.deleteComment(commentId, loginUser.getId());
            return "redirect:/recipe/user-recipe/" + recipeId;
        } catch (SecurityException e) {
            return "redirect:/recipe/user-recipe/" + recipeId + "?error=unauthorized";
        }
    }

    // ==================== 랜덤 레시피 추천 ====================

    /**
     * "오늘 뭐 먹지?" 랜덤 레시피 추천 페이지
     */
    @GetMapping("/random")
    public String randomRecipe(Model model) {
        try {
            // 레시피가 있는지 확인
            if (!userRecipeService.canRecommendRandom()) {
                model.addAttribute("error", "추천할 수 있는 레시피가 없습니다. 레시피를 먼저 등록해주세요!");
                model.addAttribute("recipes", new ArrayList<>());
                return "randomRecipe";
            }

            // API 레시피에서 랜덤 1개 추천
            List<UserRecipeDTO> recipes = userRecipeService.getRandomApiRecipes(1);

            if (recipes.isEmpty()) {
                model.addAttribute("error", "추천할 수 있는 레시피가 없습니다.");
                model.addAttribute("recipes", new ArrayList<>());
            } else {
                model.addAttribute("recipe", recipes.get(0));
                model.addAttribute("recipes", recipes);
            }

            return "randomRecipe";

        } catch (Exception e) {
            model.addAttribute("error", "레시피 추천 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("recipes", new ArrayList<>());
            return "randomRecipe";
        }
    }

    /**
     * 랜덤 레시피 다시 추천 (AJAX용)
     */
    @GetMapping("/random/refresh")
    @ResponseBody
    public Map<String, Object> refreshRandomRecipe() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 레시피가 있는지 확인
            if (!userRecipeService.canRecommendRandom()) {
                response.put("success", false);
                response.put("error", "추천할 수 있는 레시피가 없습니다.");
                return response;
            }

            // API 레시피에서 랜덤 1개 추천
            List<UserRecipeDTO> recipes = userRecipeService.getRandomApiRecipes(1);

            if (recipes.isEmpty()) {
                response.put("success", false);
                response.put("error", "추천할 수 있는 레시피가 없습니다.");
            } else {
                UserRecipeDTO recipe = recipes.get(0);
                response.put("success", true);
                response.put("recipe", recipe);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "레시피 추천 중 오류가 발생했습니다.");
        }

        return response;
    }
}
