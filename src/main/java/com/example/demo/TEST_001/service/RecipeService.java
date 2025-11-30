package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.IngredientDTO;
import com.example.demo.TEST_001.dto.RecipeDTO;
import com.example.demo.TEST_001.repository.IngredientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RestTemplate restTemplate;
    private final IngredientRepository ingredientRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${recipe.api.key}")
    private String apiKey;

    @Value("${recipe.api.base-url}")
    private String baseUrl;

    /**
     * 레시피 목록 조회 (사용자 보유 식재료 기반 매칭)
     */
    public List<RecipeDTO> getRecipeList(Long userId, String rcpWay2, String rcpPat2,
                                         String searchRecipeName, String searchIngredient,
                                         int startIdx, int endIdx) {
        try {
            // 1. 사용자의 활성 식재료 목록 조회
            List<IngredientDTO> userIngredients = ingredientRepository.getList(userId);
            Set<String> userIngredientNames = userIngredients.stream()
                    .map(IngredientDTO::getIngredientName)
                    .map(String::trim)
                    .collect(Collectors.toSet());

            // 2. 식품안전나라 API 호출
            String url = buildApiUrl(rcpWay2, rcpPat2, startIdx, endIdx);
            String jsonResponse = restTemplate.getForObject(url, String.class);

            // 3. JSON 파싱
            List<RecipeDTO> recipes = parseRecipeResponse(jsonResponse);

            // 3.5. 검색 필터 적용 (레시피명, 재료명)
            if (searchRecipeName != null && !searchRecipeName.trim().isEmpty()) {
                String searchKeyword = searchRecipeName.trim().toLowerCase();
                recipes = recipes.stream()
                        .filter(r -> r.getRcpNm() != null &&
                                r.getRcpNm().toLowerCase().contains(searchKeyword))
                        .collect(Collectors.toList());
            }

            if (searchIngredient != null && !searchIngredient.trim().isEmpty()) {
                String searchKeyword = searchIngredient.trim().toLowerCase();
                recipes = recipes.stream()
                        .filter(r -> r.getRcpPartsDtls() != null &&
                                r.getRcpPartsDtls().toLowerCase().contains(searchKeyword))
                        .collect(Collectors.toList());
            }

            // 4. 매칭 점수 계산
            for (RecipeDTO recipe : recipes) {
                calculateMatchScore(recipe, userIngredientNames);
            }

            // 5. 매칭 점수 내림차순 정렬
            recipes.sort((r1, r2) -> {
                // 매칭 점수 우선 비교
                int scoreCompare = Double.compare(r2.getMatchScore(), r1.getMatchScore());
                if (scoreCompare != 0) {
                    return scoreCompare;
                }
                // 점수가 같으면 매칭된 재료 개수로 비교
                return Integer.compare(r2.getMatchedIngredientCount(), r1.getMatchedIngredientCount());
            });

            return recipes;

        } catch (HttpClientErrorException e) {
            log.error("API 클라이언트 오류 발생 (상태 코드: {}): {}", e.getStatusCode(), e.getMessage());
            return new ArrayList<>();
        } catch (HttpServerErrorException e) {
            log.error("API 서버 오류 발생 (상태 코드: {}): {}", e.getStatusCode(), e.getMessage());
            return new ArrayList<>();
        } catch (ResourceAccessException e) {
            log.error("API 연결 오류 (타임아웃 또는 네트워크 문제): {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("레시피 조회 중 예상치 못한 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 특정 레시피 상세 조회
     */
    public RecipeDTO getRecipeDetail(String rcpSeq, Long userId) {
        try {
            // API 호출
            String url = String.format("%s/%s/COOKRCP01/json/1/1/RCP_SEQ=%s", baseUrl, apiKey, rcpSeq);
            String jsonResponse = restTemplate.getForObject(url, String.class);

            // JSON 파싱
            List<RecipeDTO> recipes = parseRecipeResponse(jsonResponse);
            if (recipes.isEmpty()) {
                return null;
            }

            RecipeDTO recipe = recipes.get(0);

            // 사용자 식재료 기반 매칭 정보 추가
            List<IngredientDTO> userIngredients = ingredientRepository.getList(userId);
            Set<String> userIngredientNames = userIngredients.stream()
                    .map(IngredientDTO::getIngredientName)
                    .map(String::trim)
                    .collect(Collectors.toSet());

            calculateMatchScore(recipe, userIngredientNames);

            return recipe;

        } catch (HttpClientErrorException e) {
            log.error("레시피 상세 조회 - API 클라이언트 오류 (상태 코드: {}): {}", e.getStatusCode(), e.getMessage());
            return null;
        } catch (HttpServerErrorException e) {
            log.error("레시피 상세 조회 - API 서버 오류 (상태 코드: {}): {}", e.getStatusCode(), e.getMessage());
            return null;
        } catch (ResourceAccessException e) {
            log.error("레시피 상세 조회 - API 연결 오류: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("레시피 상세 조회 중 예상치 못한 오류 발생", e);
            return null;
        }
    }

    /**
     * API URL 생성
     */
    private String buildApiUrl(String rcpWay2, String rcpPat2, int startIdx, int endIdx) {
        StringBuilder url = new StringBuilder();
        url.append(baseUrl).append("/").append(apiKey).append("/COOKRCP01/json/")
                .append(startIdx).append("/").append(endIdx);

        List<String> params = new ArrayList<>();
        if (rcpWay2 != null && !rcpWay2.isEmpty()) {
            params.add("RCP_WAY2=" + rcpWay2);
        }
        if (rcpPat2 != null && !rcpPat2.isEmpty()) {
            params.add("RCP_PAT2=" + rcpPat2);
        }

        if (!params.isEmpty()) {
            url.append("/").append(String.join("&", params));
        }

        return url.toString();
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
     * 레시피와 사용자 식재료 매칭 점수 계산
     * - 완전 일치: 10점
     * - 부분 일치: 3점
     */
    private void calculateMatchScore(RecipeDTO recipe, Set<String> userIngredientNames) {
        String rcpPartsDtls = recipe.getRcpPartsDtls();
        if (rcpPartsDtls == null || rcpPartsDtls.isEmpty()) {
            recipe.setMatchScore(0);
            recipe.setMatchedIngredientCount(0);
            recipe.setTotalIngredientCount(0);
            return;
        }

        // 레시피 재료 파싱 (쉼표, 줄바꿈 등으로 구분)
        String[] recipeIngredients = rcpPartsDtls.split("[,\n•·]");
        Set<String> recipeIngredientSet = new HashSet<>();
        for (String ingredient : recipeIngredients) {
            String cleaned = ingredient.trim()
                    .replaceAll("\\d+.*", "")  // 숫자 제거
                    .replaceAll("[()]", "")     // 괄호 제거
                    .trim();
            if (!cleaned.isEmpty()) {
                recipeIngredientSet.add(cleaned);
            }
        }

        int matchedCount = 0;
        double totalScore = 0;
        List<String> matchedIngredients = new ArrayList<>();

        // 각 레시피 재료에 대해 매칭 검사
        for (String recipeIngredient : recipeIngredientSet) {
            boolean exactMatch = false;
            boolean partialMatch = false;

            // 1. 완전 일치 체크 (우선순위)
            for (String userIngredient : userIngredientNames) {
                if (recipeIngredient.equals(userIngredient)) {
                    exactMatch = true;
                    matchedIngredients.add(userIngredient);
                    break;
                }
            }

            // 2. 부분 일치 체크 (완전 일치가 없을 때만)
            if (!exactMatch) {
                for (String userIngredient : userIngredientNames) {
                    if (recipeIngredient.contains(userIngredient) || userIngredient.contains(recipeIngredient)) {
                        partialMatch = true;
                        matchedIngredients.add(userIngredient + "(부분)");
                        break;
                    }
                }
            }

            // 점수 계산
            if (exactMatch) {
                matchedCount++;
                totalScore += 10;  // 완전 일치: 10점
            } else if (partialMatch) {
                matchedCount++;
                totalScore += 3;   // 부분 일치: 3점
            }
        }

        recipe.setMatchedIngredientCount(matchedCount);
        recipe.setTotalIngredientCount(recipeIngredientSet.size());
        recipe.setMatchScore(totalScore);
        recipe.setMatchedIngredients(String.join(", ", matchedIngredients));
    }
}
