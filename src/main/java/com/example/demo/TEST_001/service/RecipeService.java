package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.IngredientDTO;
import com.example.demo.TEST_001.dto.RecipeDTO;
import com.example.demo.TEST_001.dto.UserRecipeDTO;
import com.example.demo.TEST_001.repository.IngredientRepository;
import com.example.demo.TEST_001.repository.UserRecipeMatchRepository;
import com.example.demo.TEST_001.repository.UserRecipeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RestTemplate restTemplate;
    private final IngredientRepository ingredientRepository;
    private final UserRecipeRepository userRecipeRepository;
    private final UserRecipeMatchRepository userRecipeMatchRepository;
    private final RecipeMatchService recipeMatchService;
    private final SqlSessionTemplate sql;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${recipe.api.key}")
    private String apiKey;

    @Value("${recipe.api.base-url}")
    private String baseUrl;

    /**
     * 레시피 목록 조회 + 전체 개수 (DB 기반 - 성능 최적화 버전)
     * 미리 계산된 매칭 점수를 사용하여 DB에서 바로 정렬 + 페이징
     */
    public Map<String, Object> getRecipeListWithCount(Long userId, String rcpWay2, String rcpPat2,
                                                       String searchRecipeName, String searchIngredient,
                                                       int page, int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 매칭 점수가 계산되어 있지 않으면 계산 (최초 1회만)
            if (!recipeMatchService.hasMatchScores(userId)) {
                log.info("사용자 {} 매칭 점수 최초 계산 시작", userId);
                recipeMatchService.recalculateMatchScores(userId);
            }

            // 2. 페이징 파라미터 계산
            int offset = (page - 1) * size;

            // 3. DB에서 정렬 + 페이징된 결과 바로 조회
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("rcpWay2", rcpWay2);
            params.put("rcpPat2", rcpPat2);
            params.put("searchRecipeName", searchRecipeName);
            params.put("searchIngredient", searchIngredient);
            params.put("limit", size);
            params.put("offset", offset);

            List<UserRecipeDTO> recipes = sql.selectList("userRecipeMatch.findApiRecipesWithMatch", params);

            // 4. 전체 개수 조회 (필터 적용)
            int totalCount = sql.selectOne("userRecipeMatch.countApiRecipesFiltered", params);

            result.put("recipes", recipes);
            result.put("totalCount", totalCount);

        } catch (Exception e) {
            log.error("레시피 조회 중 오류 발생", e);
            result.put("recipes", new ArrayList<>());
            result.put("totalCount", 0);
        }

        return result;
    }

    /**
     * 특정 레시피 상세 조회 (DB 기반)
     */
    public UserRecipeDTO getRecipeDetail(String rcpSeq, Long userId) {
        try {
            // DB에서 조회
            UserRecipeDTO recipe = userRecipeRepository.findByRcpSeq(rcpSeq);
            if (recipe == null) {
                return null;
            }

            // 사용자 식재료 기반 매칭 정보 추가
            List<IngredientDTO> userIngredients = ingredientRepository.getList(userId);
            Set<String> userIngredientNames = userIngredients.stream()
                    .map(IngredientDTO::getIngredientName)
                    .map(String::trim)
                    .collect(Collectors.toSet());

            calculateMatchScoreForUserRecipe(recipe, userIngredientNames);

            return recipe;

        } catch (Exception e) {
            log.error("레시피 상세 조회 중 오류 발생", e);
            return null;
        }
    }

    /**
     * API 레시피 데이터 동기화 (DB에 저장)
     */
    public void syncApiRecipes() {
        log.info("API 레시피 데이터 동기화 시작...");

        try {
            int existingCount = userRecipeRepository.countApiRecipes();
            if (existingCount > 0) {
                log.info("이미 {} 개의 API 레시피가 DB에 저장되어 있습니다. 동기화를 건너뜁니다.", existingCount);
                return;
            }

            int batchSize = 100;
            int startIdx = 1;
            int totalSaved = 0;

            while (true) {
                int endIdx = startIdx + batchSize - 1;
                String url = String.format("%s/%s/COOKRCP01/json/%d/%d", baseUrl, apiKey, startIdx, endIdx);

                log.info("API 호출: {} ~ {}", startIdx, endIdx);
                String jsonResponse = restTemplate.getForObject(url, String.class);

                List<RecipeDTO> recipes = parseRecipeResponse(jsonResponse);
                if (recipes.isEmpty()) {
                    log.info("더 이상 레시피가 없습니다. 동기화 완료.");
                    break;
                }

                for (RecipeDTO apiRecipe : recipes) {
                    // 중복 체크
                    if (userRecipeRepository.existsByRcpSeq(apiRecipe.getRcpSeq())) {
                        continue;
                    }

                    // RecipeDTO -> UserRecipeDTO 변환 후 저장
                    UserRecipeDTO userRecipe = convertToUserRecipeDTO(apiRecipe);
                    userRecipeRepository.saveApiRecipe(userRecipe);
                    totalSaved++;
                }

                log.info("{} 개 저장 완료 (총 {}개)", recipes.size(), totalSaved);

                // 다음 배치
                startIdx = endIdx + 1;

                // API 호출 간격 두기 (Rate Limit 방지)
                Thread.sleep(500);
            }

            log.info("API 레시피 동기화 완료. 총 {}개 저장됨.", totalSaved);

        } catch (Exception e) {
            log.error("API 레시피 동기화 중 오류 발생", e);
        }
    }

    /**
     * RecipeDTO -> UserRecipeDTO 변환 (재료 미리 파싱하여 저장)
     */
    private UserRecipeDTO convertToUserRecipeDTO(RecipeDTO apiRecipe) {
        UserRecipeDTO dto = new UserRecipeDTO();
        dto.setSource("api");
        dto.setRcpSeq(apiRecipe.getRcpSeq());
        dto.setTitle(apiRecipe.getRcpNm());
        dto.setRcpWay2(apiRecipe.getRcpWay2());
        dto.setRcpPat2(apiRecipe.getRcpPat2());
        dto.setRcpPartsDtls(apiRecipe.getRcpPartsDtls());
        dto.setInfoWgt(apiRecipe.getInfoWgt());
        dto.setInfoEng(apiRecipe.getInfoEng());
        dto.setInfoCar(apiRecipe.getInfoCar());
        dto.setInfoPro(apiRecipe.getInfoPro());
        dto.setInfoFat(apiRecipe.getInfoFat());
        dto.setInfoNa(apiRecipe.getInfoNa());
        dto.setRcpNaTip(apiRecipe.getRcpNaTip());
        dto.setHashTag(apiRecipe.getHashTag());
        dto.setAttFileNoMain(apiRecipe.getAttFileNoMain());
        dto.setAttFileNoMk(apiRecipe.getAttFileNoMk());

        // 재료 미리 파싱하여 JSON으로 저장 (성능 최적화)
        String rcpPartsDtls = apiRecipe.getRcpPartsDtls();
        if (rcpPartsDtls != null && !rcpPartsDtls.isEmpty()) {
            Set<String> ingredientSet = new HashSet<>();
            String[] ingredients = rcpPartsDtls.split("[,\n•·]");
            for (String ingredient : ingredients) {
                String cleaned = ingredient.trim()
                        .replaceAll("\\d+.*", "")
                        .replaceAll("[()]", "")
                        .trim();
                if (!cleaned.isEmpty()) {
                    ingredientSet.add(cleaned);
                }
            }
            // JSON 배열로 변환
            try {
                dto.setParsedIngredients(objectMapper.writeValueAsString(ingredientSet));
                dto.setIngredientCount(ingredientSet.size());
            } catch (Exception e) {
                dto.setParsedIngredients("[]");
                dto.setIngredientCount(0);
            }
        } else {
            dto.setParsedIngredients("[]");
            dto.setIngredientCount(0);
        }

        // 조리 단계
        dto.setManual01(apiRecipe.getManual01());
        dto.setManual02(apiRecipe.getManual02());
        dto.setManual03(apiRecipe.getManual03());
        dto.setManual04(apiRecipe.getManual04());
        dto.setManual05(apiRecipe.getManual05());
        dto.setManual06(apiRecipe.getManual06());
        dto.setManual07(apiRecipe.getManual07());
        dto.setManual08(apiRecipe.getManual08());
        dto.setManual09(apiRecipe.getManual09());
        dto.setManual10(apiRecipe.getManual10());
        dto.setManual11(apiRecipe.getManual11());
        dto.setManual12(apiRecipe.getManual12());
        dto.setManual13(apiRecipe.getManual13());
        dto.setManual14(apiRecipe.getManual14());
        dto.setManual15(apiRecipe.getManual15());
        dto.setManual16(apiRecipe.getManual16());
        dto.setManual17(apiRecipe.getManual17());
        dto.setManual18(apiRecipe.getManual18());
        dto.setManual19(apiRecipe.getManual19());
        dto.setManual20(apiRecipe.getManual20());

        // 조리 단계 이미지
        dto.setManualImg01(apiRecipe.getManualImg01());
        dto.setManualImg02(apiRecipe.getManualImg02());
        dto.setManualImg03(apiRecipe.getManualImg03());
        dto.setManualImg04(apiRecipe.getManualImg04());
        dto.setManualImg05(apiRecipe.getManualImg05());
        dto.setManualImg06(apiRecipe.getManualImg06());
        dto.setManualImg07(apiRecipe.getManualImg07());
        dto.setManualImg08(apiRecipe.getManualImg08());
        dto.setManualImg09(apiRecipe.getManualImg09());
        dto.setManualImg10(apiRecipe.getManualImg10());
        dto.setManualImg11(apiRecipe.getManualImg11());
        dto.setManualImg12(apiRecipe.getManualImg12());
        dto.setManualImg13(apiRecipe.getManualImg13());
        dto.setManualImg14(apiRecipe.getManualImg14());
        dto.setManualImg15(apiRecipe.getManualImg15());
        dto.setManualImg16(apiRecipe.getManualImg16());
        dto.setManualImg17(apiRecipe.getManualImg17());
        dto.setManualImg18(apiRecipe.getManualImg18());
        dto.setManualImg19(apiRecipe.getManualImg19());
        dto.setManualImg20(apiRecipe.getManualImg20());

        return dto;
    }

    /**
     * JSON 응답 파싱
     */
    private List<RecipeDTO> parseRecipeResponse(String jsonResponse) throws Exception {
        List<RecipeDTO> recipes = new ArrayList<>();

        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode cookrcp01 = root.path("COOKRCP01");
        JsonNode totalCount = cookrcp01.path("total_count");

        // 데이터가 없는 경우
        if (totalCount.isMissingNode() || totalCount.asInt() == 0) {
            return recipes;
        }

        JsonNode rows = cookrcp01.path("row");
        if (rows.isArray()) {
            for (JsonNode row : rows) {
                RecipeDTO recipe = new RecipeDTO();

                // 기본 정보
                recipe.setRcpSeq(row.path("RCP_SEQ").asText());
                recipe.setRcpNm(row.path("RCP_NM").asText());
                recipe.setRcpWay2(row.path("RCP_WAY2").asText());
                recipe.setRcpPat2(row.path("RCP_PAT2").asText());

                // 영양정보
                recipe.setInfoWgt(row.path("INFO_WGT").asText());
                recipe.setInfoEng(row.path("INFO_ENG").asText());
                recipe.setInfoCar(row.path("INFO_CAR").asText());
                recipe.setInfoPro(row.path("INFO_PRO").asText());
                recipe.setInfoFat(row.path("INFO_FAT").asText());
                recipe.setInfoNa(row.path("INFO_NA").asText());

                // 재료정보
                recipe.setRcpPartsDtls(row.path("RCP_PARTS_DTLS").asText());

                // 조리법 단계별 설명
                recipe.setManual01(row.path("MANUAL01").asText());
                recipe.setManual02(row.path("MANUAL02").asText());
                recipe.setManual03(row.path("MANUAL03").asText());
                recipe.setManual04(row.path("MANUAL04").asText());
                recipe.setManual05(row.path("MANUAL05").asText());
                recipe.setManual06(row.path("MANUAL06").asText());
                recipe.setManual07(row.path("MANUAL07").asText());
                recipe.setManual08(row.path("MANUAL08").asText());
                recipe.setManual09(row.path("MANUAL09").asText());
                recipe.setManual10(row.path("MANUAL10").asText());
                recipe.setManual11(row.path("MANUAL11").asText());
                recipe.setManual12(row.path("MANUAL12").asText());
                recipe.setManual13(row.path("MANUAL13").asText());
                recipe.setManual14(row.path("MANUAL14").asText());
                recipe.setManual15(row.path("MANUAL15").asText());
                recipe.setManual16(row.path("MANUAL16").asText());
                recipe.setManual17(row.path("MANUAL17").asText());
                recipe.setManual18(row.path("MANUAL18").asText());
                recipe.setManual19(row.path("MANUAL19").asText());
                recipe.setManual20(row.path("MANUAL20").asText());

                // 조리법 단계별 이미지
                recipe.setManualImg01(row.path("MANUAL_IMG01").asText());
                recipe.setManualImg02(row.path("MANUAL_IMG02").asText());
                recipe.setManualImg03(row.path("MANUAL_IMG03").asText());
                recipe.setManualImg04(row.path("MANUAL_IMG04").asText());
                recipe.setManualImg05(row.path("MANUAL_IMG05").asText());
                recipe.setManualImg06(row.path("MANUAL_IMG06").asText());
                recipe.setManualImg07(row.path("MANUAL_IMG07").asText());
                recipe.setManualImg08(row.path("MANUAL_IMG08").asText());
                recipe.setManualImg09(row.path("MANUAL_IMG09").asText());
                recipe.setManualImg10(row.path("MANUAL_IMG10").asText());
                recipe.setManualImg11(row.path("MANUAL_IMG11").asText());
                recipe.setManualImg12(row.path("MANUAL_IMG12").asText());
                recipe.setManualImg13(row.path("MANUAL_IMG13").asText());
                recipe.setManualImg14(row.path("MANUAL_IMG14").asText());
                recipe.setManualImg15(row.path("MANUAL_IMG15").asText());
                recipe.setManualImg16(row.path("MANUAL_IMG16").asText());
                recipe.setManualImg17(row.path("MANUAL_IMG17").asText());
                recipe.setManualImg18(row.path("MANUAL_IMG18").asText());
                recipe.setManualImg19(row.path("MANUAL_IMG19").asText());
                recipe.setManualImg20(row.path("MANUAL_IMG20").asText());

                // 이미지
                recipe.setAttFileNoMain(row.path("ATT_FILE_NO_MAIN").asText());
                recipe.setAttFileNoMk(row.path("ATT_FILE_NO_MK").asText());

                // 기타
                recipe.setRcpNaTip(row.path("RCP_NA_TIP").asText());
                recipe.setHashTag(row.path("HASH_TAG").asText());

                recipes.add(recipe);
            }
        }

        return recipes;
    }

    /**
     * 레시피와 사용자 식재료 매칭 점수 계산 (UserRecipeDTO용) - 성능 최적화 버전
     */
    private void calculateMatchScoreForUserRecipe(UserRecipeDTO recipe, Set<String> userIngredientNames) {
        Set<String> recipeIngredientSet = new HashSet<>();

        // 1. 미리 파싱된 JSON이 있으면 사용 (성능 최적화)
        String parsedIngredients = recipe.getParsedIngredients();
        if (parsedIngredients != null && !parsedIngredients.isEmpty() && !parsedIngredients.equals("[]")) {
            try {
                // JSON 배열 파싱
                JsonNode jsonArray = objectMapper.readTree(parsedIngredients);
                if (jsonArray.isArray()) {
                    for (JsonNode node : jsonArray) {
                        String ingredient = node.asText().trim();
                        if (!ingredient.isEmpty()) {
                            recipeIngredientSet.add(ingredient);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("parsedIngredients JSON 파싱 실패, rcpPartsDtls 사용: {}", e.getMessage());
                // JSON 파싱 실패시 기존 방식으로 폴백
                parseRcpPartsDtls(recipe.getRcpPartsDtls(), recipeIngredientSet);
            }
        } else {
            // 2. 파싱된 데이터가 없으면 기존 방식 사용
            parseRcpPartsDtls(recipe.getRcpPartsDtls(), recipeIngredientSet);
        }

        if (recipeIngredientSet.isEmpty()) {
            recipe.setMatchScore(0);
            recipe.setMatchedIngredientCount(0);
            recipe.setTotalIngredientCount(0);
            return;
        }

        int matchedCount = 0;
        double totalScore = 0;
        List<String> matchedIngredients = new ArrayList<>();

        // 각 레시피 재료에 대해 매칭 검사 (최적화: O(n) 완전일치 + 부분일치만 O(n×m))
        for (String recipeIngredient : recipeIngredientSet) {
            // 1. 완전 일치 체크 - HashSet.contains()는 O(1)
            if (userIngredientNames.contains(recipeIngredient)) {
                matchedCount++;
                totalScore += 10;
                matchedIngredients.add(recipeIngredient);
                continue;
            }

            // 2. 부분 일치 체크 (완전 일치가 없을 때만)
            for (String userIngredient : userIngredientNames) {
                if (recipeIngredient.contains(userIngredient) || userIngredient.contains(recipeIngredient)) {
                    matchedCount++;
                    totalScore += 3;
                    matchedIngredients.add(userIngredient + "(부분)");
                    break;
                }
            }
        }

        recipe.setMatchedIngredientCount(matchedCount);
        recipe.setTotalIngredientCount(recipeIngredientSet.size());
        recipe.setMatchScore(totalScore);
        recipe.setMatchedIngredients(String.join(", ", matchedIngredients));
    }

    /**
     * rcpPartsDtls 문자열을 파싱하여 재료 Set에 추가 (폴백용)
     */
    private void parseRcpPartsDtls(String rcpPartsDtls, Set<String> recipeIngredientSet) {
        if (rcpPartsDtls == null || rcpPartsDtls.isEmpty()) {
            return;
        }
        String[] recipeIngredients = rcpPartsDtls.split("[,\n•·]");
        for (String ingredient : recipeIngredients) {
            String cleaned = ingredient.trim()
                    .replaceAll("\\d+.*", "")
                    .replaceAll("[()]", "")
                    .trim();
            if (!cleaned.isEmpty()) {
                recipeIngredientSet.add(cleaned);
            }
        }
    }

    // ========================================
    // 통합 레시피 조회 (API + 사용자)
    // ========================================

    /**
     * 통합 레시피 목록 조회 (API + 사용자, 매칭 점수 포함)
     */
    public Map<String, Object> getIntegratedRecipeListWithCount(
            Long userId, String source, String rcpWay2, String rcpPat2,
            String searchRecipeName, String searchIngredient, String searchAuthor,
            int page, int size) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 매칭 점수가 계산되어 있지 않으면 계산 (최초 1회만)
            if (userId != null && !recipeMatchService.hasMatchScores(userId)) {
                log.info("사용자 {} 매칭 점수 최초 계산 시작", userId);
                recipeMatchService.recalculateMatchScores(userId);
            }

            // 2. 페이징 파라미터 계산
            int offset = (page - 1) * size;

            // 3. DB에서 정렬 + 페이징된 결과 바로 조회
            List<UserRecipeDTO> recipes = userRecipeMatchRepository.findIntegratedRecipesWithMatch(
                    userId, source, rcpWay2, rcpPat2,
                    searchRecipeName, searchIngredient, searchAuthor,
                    size, offset);

            // 4. 전체 개수 조회 (필터 적용)
            int totalCount = userRecipeMatchRepository.countIntegratedRecipesFiltered(
                    source, rcpWay2, rcpPat2,
                    searchRecipeName, searchIngredient, searchAuthor);

            result.put("recipes", recipes);
            result.put("totalCount", totalCount);

        } catch (Exception e) {
            log.error("통합 레시피 조회 중 오류 발생", e);
            result.put("recipes", new ArrayList<>());
            result.put("totalCount", 0);
        }

        return result;
    }

    /**
     * 통합 레시피 상세 조회 (ID 기반)
     */
    public UserRecipeDTO getIntegratedRecipeDetail(Long id, Long userId) {
        try {
            UserRecipeDTO recipe = userRecipeRepository.findByIdIntegrated(id, userId);
            if (recipe == null) {
                return null;
            }

            // 조회수 증가
            userRecipeRepository.incrementViewCount(id);

            return recipe;

        } catch (Exception e) {
            log.error("통합 레시피 상세 조회 중 오류 발생", e);
            return null;
        }
    }
}
