package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.FoodRecognitionResultDTO;
import com.example.demo.TEST_001.dto.PredictionDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FoodRecognitionService {

    private final RestTemplate huggingFaceRestTemplate;
    private final FileUploadService fileUploadService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${huggingface.api.key}")
    private String apiKey;

    @Value("${huggingface.api.base-url}")
    private String baseUrl;

    @Value("${huggingface.api.model}")
    private String modelId;

    // 영문 -> 한글 매핑 (음식만)
    private static final Map<String, String> LABEL_KOREAN_MAP = new LinkedHashMap<>();
    // 음식 라벨 Set (음식 여부 판단용)
    private static final Set<String> FOOD_LABELS = new HashSet<>();
    // 영문 -> 카테고리 ID 매핑 (기존 DB의 category 테이블 참고)
    private static final Map<String, Integer> LABEL_CATEGORY_MAP = new HashMap<>();
    // 라벨별 추천 식재료
    private static final Map<String, List<String>> LABEL_SUGGESTIONS = new HashMap<>();

    static {
        // ========== ImageNet-1k 음식 관련 클래스 전체 매핑 ==========

        // 과일류
        LABEL_KOREAN_MAP.put("banana", "바나나");
        LABEL_KOREAN_MAP.put("orange", "오렌지");
        LABEL_KOREAN_MAP.put("lemon", "레몬");
        LABEL_KOREAN_MAP.put("fig", "무화과");
        LABEL_KOREAN_MAP.put("pineapple", "파인애플");
        LABEL_KOREAN_MAP.put("strawberry", "딸기");
        LABEL_KOREAN_MAP.put("pomegranate", "석류");
        LABEL_KOREAN_MAP.put("custard apple", "슈가애플");
        LABEL_KOREAN_MAP.put("Granny Smith", "사과");
        LABEL_KOREAN_MAP.put("jackfruit", "잭프루트");
        LABEL_KOREAN_MAP.put("acorn", "도토리");

        // 채소류
        LABEL_KOREAN_MAP.put("broccoli", "브로콜리");
        LABEL_KOREAN_MAP.put("cauliflower", "콜리플라워");
        LABEL_KOREAN_MAP.put("cucumber", "오이");
        LABEL_KOREAN_MAP.put("zucchini", "주키니호박");
        LABEL_KOREAN_MAP.put("artichoke", "아티초크");
        LABEL_KOREAN_MAP.put("bell pepper", "피망");
        LABEL_KOREAN_MAP.put("cardoon", "카르돈");
        LABEL_KOREAN_MAP.put("mushroom", "버섯");
        LABEL_KOREAN_MAP.put("agaric", "주름버섯");
        LABEL_KOREAN_MAP.put("hen-of-the-woods", "잎새버섯");
        LABEL_KOREAN_MAP.put("bolete", "그물버섯");
        LABEL_KOREAN_MAP.put("ear", "목이버섯");
        LABEL_KOREAN_MAP.put("coral fungus", "싸리버섯");
        LABEL_KOREAN_MAP.put("head cabbage", "양배추");
        LABEL_KOREAN_MAP.put("spaghetti squash", "스파게티호박");
        LABEL_KOREAN_MAP.put("butternut squash", "버터넛호박");
        LABEL_KOREAN_MAP.put("acorn squash", "도토리호박");
        LABEL_KOREAN_MAP.put("corn", "옥수수");

        // 음식/요리
        LABEL_KOREAN_MAP.put("bagel", "베이글");
        LABEL_KOREAN_MAP.put("pretzel", "프레첼");
        LABEL_KOREAN_MAP.put("cheeseburger", "치즈버거");
        LABEL_KOREAN_MAP.put("hotdog", "핫도그");
        LABEL_KOREAN_MAP.put("hot dog", "핫도그");
        LABEL_KOREAN_MAP.put("pizza", "피자");
        LABEL_KOREAN_MAP.put("burrito", "부리토");
        LABEL_KOREAN_MAP.put("meat loaf", "미트로프");
        LABEL_KOREAN_MAP.put("meatloaf", "미트로프");
        LABEL_KOREAN_MAP.put("French loaf", "바게트");
        LABEL_KOREAN_MAP.put("guacamole", "과카몰리");
        LABEL_KOREAN_MAP.put("consomme", "콩소메");
        LABEL_KOREAN_MAP.put("potpie", "팟파이");
        LABEL_KOREAN_MAP.put("carbonara", "카르보나라");
        LABEL_KOREAN_MAP.put("dough", "반죽");

        // 디저트/과자류
        LABEL_KOREAN_MAP.put("ice cream", "아이스크림");
        LABEL_KOREAN_MAP.put("ice lolly", "아이스바");
        LABEL_KOREAN_MAP.put("chocolate sauce", "초콜릿소스");
        LABEL_KOREAN_MAP.put("trifle", "트라이플");

        // 음료
        LABEL_KOREAN_MAP.put("espresso", "에스프레소");
        LABEL_KOREAN_MAP.put("eggnog", "에그노그");
        LABEL_KOREAN_MAP.put("red wine", "레드와인");

        // 해산물
        LABEL_KOREAN_MAP.put("lobster", "랍스터");
        LABEL_KOREAN_MAP.put("American lobster", "아메리칸랍스터");
        LABEL_KOREAN_MAP.put("spiny lobster", "가시랍스터");
        LABEL_KOREAN_MAP.put("crayfish", "가재");
        LABEL_KOREAN_MAP.put("Dungeness crab", "던지니스크랩");
        LABEL_KOREAN_MAP.put("rock crab", "돌게");
        LABEL_KOREAN_MAP.put("fiddler crab", "농게");
        LABEL_KOREAN_MAP.put("king crab", "킹크랩");
        LABEL_KOREAN_MAP.put("hermit crab", "소라게");
        LABEL_KOREAN_MAP.put("isopod", "등각류");
        LABEL_KOREAN_MAP.put("conch", "소라");
        LABEL_KOREAN_MAP.put("snail", "달팽이");
        LABEL_KOREAN_MAP.put("slug", "민달팽이");
        LABEL_KOREAN_MAP.put("chiton", "군부");
        LABEL_KOREAN_MAP.put("sea slug", "갯민숭달팽이");
        LABEL_KOREAN_MAP.put("sea cucumber", "해삼");
        LABEL_KOREAN_MAP.put("sea urchin", "성게");
        LABEL_KOREAN_MAP.put("starfish", "불가사리");

        // 생선
        LABEL_KOREAN_MAP.put("goldfish", "물고기");
        LABEL_KOREAN_MAP.put("tench", "잉어");
        LABEL_KOREAN_MAP.put("gar", "가아");
        LABEL_KOREAN_MAP.put("lionfish", "쏠배감펭");
        LABEL_KOREAN_MAP.put("puffer", "복어");
        LABEL_KOREAN_MAP.put("sturgeon", "철갑상어");
        LABEL_KOREAN_MAP.put("eel", "장어");
        LABEL_KOREAN_MAP.put("coho", "은연어");
        LABEL_KOREAN_MAP.put("barracouta", "꼬치고기");

        // 고기/가금류
        LABEL_KOREAN_MAP.put("hen", "닭");
        LABEL_KOREAN_MAP.put("cock", "수탉");
        LABEL_KOREAN_MAP.put("turkey", "칠면조");
        LABEL_KOREAN_MAP.put("goose", "거위");
        LABEL_KOREAN_MAP.put("drake", "수오리");
        LABEL_KOREAN_MAP.put("red-breasted merganser", "바다비오리");

        // 음식 라벨 Set 초기화 (LABEL_KOREAN_MAP의 모든 키)
        FOOD_LABELS.addAll(LABEL_KOREAN_MAP.keySet());

        // ========== 카테고리 ID 매핑 (DB category 테이블 기준) ==========
        // 1:채소, 2:육류, 3:유제품, 4:과일, 5:조미료, 6:기타

        // 과일 (4)
        LABEL_CATEGORY_MAP.put("banana", 4);
        LABEL_CATEGORY_MAP.put("orange", 4);
        LABEL_CATEGORY_MAP.put("lemon", 4);
        LABEL_CATEGORY_MAP.put("fig", 4);
        LABEL_CATEGORY_MAP.put("pineapple", 4);
        LABEL_CATEGORY_MAP.put("strawberry", 4);
        LABEL_CATEGORY_MAP.put("pomegranate", 4);
        LABEL_CATEGORY_MAP.put("custard apple", 4);
        LABEL_CATEGORY_MAP.put("Granny Smith", 4);
        LABEL_CATEGORY_MAP.put("jackfruit", 4);

        // 채소 (1)
        LABEL_CATEGORY_MAP.put("broccoli", 1);
        LABEL_CATEGORY_MAP.put("cauliflower", 1);
        LABEL_CATEGORY_MAP.put("cucumber", 1);
        LABEL_CATEGORY_MAP.put("zucchini", 1);
        LABEL_CATEGORY_MAP.put("artichoke", 1);
        LABEL_CATEGORY_MAP.put("bell pepper", 1);
        LABEL_CATEGORY_MAP.put("cardoon", 1);
        LABEL_CATEGORY_MAP.put("mushroom", 1);
        LABEL_CATEGORY_MAP.put("agaric", 1);
        LABEL_CATEGORY_MAP.put("hen-of-the-woods", 1);
        LABEL_CATEGORY_MAP.put("bolete", 1);
        LABEL_CATEGORY_MAP.put("ear", 1);
        LABEL_CATEGORY_MAP.put("coral fungus", 1);
        LABEL_CATEGORY_MAP.put("head cabbage", 1);
        LABEL_CATEGORY_MAP.put("spaghetti squash", 1);
        LABEL_CATEGORY_MAP.put("butternut squash", 1);
        LABEL_CATEGORY_MAP.put("acorn squash", 1);
        LABEL_CATEGORY_MAP.put("corn", 1);

        // 육류/가금류 (2)
        LABEL_CATEGORY_MAP.put("cheeseburger", 2);
        LABEL_CATEGORY_MAP.put("hotdog", 2);
        LABEL_CATEGORY_MAP.put("hot dog", 2);
        LABEL_CATEGORY_MAP.put("meat loaf", 2);
        LABEL_CATEGORY_MAP.put("meatloaf", 2);
        LABEL_CATEGORY_MAP.put("hen", 2);
        LABEL_CATEGORY_MAP.put("cock", 2);
        LABEL_CATEGORY_MAP.put("turkey", 2);
        LABEL_CATEGORY_MAP.put("goose", 2);
        LABEL_CATEGORY_MAP.put("drake", 2);

        // 해산물 - 기타로 분류 (6)
        LABEL_CATEGORY_MAP.put("lobster", 6);
        LABEL_CATEGORY_MAP.put("American lobster", 6);
        LABEL_CATEGORY_MAP.put("spiny lobster", 6);
        LABEL_CATEGORY_MAP.put("crayfish", 6);
        LABEL_CATEGORY_MAP.put("Dungeness crab", 6);
        LABEL_CATEGORY_MAP.put("rock crab", 6);
        LABEL_CATEGORY_MAP.put("fiddler crab", 6);
        LABEL_CATEGORY_MAP.put("king crab", 6);
        LABEL_CATEGORY_MAP.put("hermit crab", 6);
        LABEL_CATEGORY_MAP.put("conch", 6);
        LABEL_CATEGORY_MAP.put("sea cucumber", 6);
        LABEL_CATEGORY_MAP.put("sea urchin", 6);
        LABEL_CATEGORY_MAP.put("eel", 6);
        LABEL_CATEGORY_MAP.put("coho", 6);
        LABEL_CATEGORY_MAP.put("sturgeon", 6);
        LABEL_CATEGORY_MAP.put("puffer", 6);

        // 빵/요리 - 기타 (6)
        LABEL_CATEGORY_MAP.put("bagel", 6);
        LABEL_CATEGORY_MAP.put("pretzel", 6);
        LABEL_CATEGORY_MAP.put("French loaf", 6);
        LABEL_CATEGORY_MAP.put("pizza", 6);
        LABEL_CATEGORY_MAP.put("burrito", 6);
        LABEL_CATEGORY_MAP.put("potpie", 6);
        LABEL_CATEGORY_MAP.put("dough", 6);

        // 기타/디저트 (6)
        LABEL_CATEGORY_MAP.put("ice cream", 6);
        LABEL_CATEGORY_MAP.put("ice lolly", 6);
        LABEL_CATEGORY_MAP.put("chocolate sauce", 6);
        LABEL_CATEGORY_MAP.put("trifle", 6);
        LABEL_CATEGORY_MAP.put("espresso", 6);
        LABEL_CATEGORY_MAP.put("eggnog", 6);
        LABEL_CATEGORY_MAP.put("red wine", 6);
        LABEL_CATEGORY_MAP.put("guacamole", 6);
        LABEL_CATEGORY_MAP.put("consomme", 6);
        LABEL_CATEGORY_MAP.put("carbonara", 6);

        // ========== 추천 식재료 ==========
        LABEL_SUGGESTIONS.put("pizza", Arrays.asList("피자도우", "모짜렐라치즈", "토마토소스", "페퍼로니"));
        LABEL_SUGGESTIONS.put("cheeseburger", Arrays.asList("소고기패티", "치즈", "빵", "양상추", "토마토"));
        LABEL_SUGGESTIONS.put("hotdog", Arrays.asList("소시지", "핫도그빵", "케첩", "머스타드"));
        LABEL_SUGGESTIONS.put("hot dog", Arrays.asList("소시지", "핫도그빵", "케첩", "머스타드"));
        LABEL_SUGGESTIONS.put("burrito", Arrays.asList("또띠아", "소고기", "콩", "치즈", "살사소스"));
        LABEL_SUGGESTIONS.put("carbonara", Arrays.asList("스파게티면", "베이컨", "계란", "파마산치즈"));
        LABEL_SUGGESTIONS.put("bagel", Arrays.asList("베이글", "크림치즈", "연어"));
        LABEL_SUGGESTIONS.put("ice cream", Arrays.asList("우유", "생크림", "설탕", "바닐라"));
        LABEL_SUGGESTIONS.put("broccoli", Arrays.asList("브로콜리", "마늘", "올리브오일"));
        LABEL_SUGGESTIONS.put("lobster", Arrays.asList("랍스터", "버터", "레몬"));
        LABEL_SUGGESTIONS.put("banana", Arrays.asList("바나나", "우유", "꿀"));
        LABEL_SUGGESTIONS.put("strawberry", Arrays.asList("딸기", "생크림", "설탕"));
        LABEL_SUGGESTIONS.put("orange", Arrays.asList("오렌지"));
        LABEL_SUGGESTIONS.put("cucumber", Arrays.asList("오이", "소금", "참기름"));
        LABEL_SUGGESTIONS.put("mushroom", Arrays.asList("버섯", "마늘", "버터"));
    }

    /**
     * 음식 라벨인지 확인
     */
    public static boolean isFoodLabel(String label) {
        return FOOD_LABELS.contains(label);
    }

    public FoodRecognitionService(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;

        // Hugging Face API용 RestTemplate (타임아웃 60초 - Cold Start 대응)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);  // 연결 타임아웃: 15초
        factory.setReadTimeout(60000);     // 읽기 타임아웃: 60초 (모델 로딩 시간)
        this.huggingFaceRestTemplate = new RestTemplate(factory);
    }

    /**
     * 이미지를 Hugging Face API로 전송하여 음식 인식
     */
    public FoodRecognitionResultDTO recognizeFood(MultipartFile file) {
        FoodRecognitionResultDTO result = new FoodRecognitionResultDTO();

        try {
            // 1. 파일 검증
            if (file.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("파일이 비어있습니다.");
                return result;
            }

            result.setOriginalFilename(file.getOriginalFilename());

            // 2. 이미지 업로드 (로컬 저장)
            try {
                String imageUrl = fileUploadService.uploadFile(file);
                result.setImageUrl(imageUrl);
            } catch (Exception e) {
                log.warn("이미지 저장 실패, 분석은 계속 진행: {}", e.getMessage());
            }

            // 3. Hugging Face API 호출
            String apiUrl = baseUrl + "/" + modelId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            log.info("Hugging Face API 호출 시작: {}", modelId);

            ResponseEntity<String> response = huggingFaceRestTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("Hugging Face API 응답 상태: {}", response.getStatusCode());

            // 4. 응답 파싱
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                log.debug("API 응답: {}", responseBody);

                List<Map<String, Object>> predictions = objectMapper.readValue(
                        responseBody,
                        new TypeReference<List<Map<String, Object>>>() {}
                );

                List<PredictionDTO> predictionList = new ArrayList<>();
                for (Map<String, Object> pred : predictions) {
                    String label = (String) pred.get("label");
                    Double score = ((Number) pred.get("score")).doubleValue();

                    PredictionDTO dto = new PredictionDTO();
                    dto.setLabel(label);

                    // 음식인지 아닌지 판단
                    if (FOOD_LABELS.contains(label)) {
                        // 음식인 경우: 한글 매핑 사용, 점수 그대로
                        dto.setKoreanLabel(LABEL_KOREAN_MAP.get(label));
                        dto.setScore(score);
                        // 카테고리 ID 설정
                        dto.setSuggestedCategoryId(LABEL_CATEGORY_MAP.getOrDefault(label, 6));
                    } else {
                        // 음식이 아닌 경우: "기타"로 표시, 점수 대폭 감소
                        dto.setKoreanLabel("기타");
                        dto.setScore(score * 0.1);  // 신뢰도 10%로 감소
                        dto.setSuggestedCategoryId(6); // 기타 카테고리
                    }
                    predictionList.add(dto);
                }

                // 음식 항목을 우선으로 정렬 (점수 높은 순)
                predictionList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

                result.setPredictions(predictionList);
                result.setSuccess(true);
                result.setMessage("인식 성공");

                // 최상위 결과 설정 (음식 항목 우선)
                PredictionDTO topFood = predictionList.stream()
                        .filter(p -> FOOD_LABELS.contains(p.getLabel()))
                        .findFirst()
                        .orElse(predictionList.isEmpty() ? null : predictionList.get(0));

                if (topFood != null) {
                    result.setTopLabel(topFood.getLabel());
                    result.setTopScore(topFood.getScore());
                    result.setKoreanLabel(topFood.getKoreanLabel());
                    result.setSuggestedCategoryId(LABEL_CATEGORY_MAP.get(topFood.getLabel()));
                }

                log.info("음식 인식 성공: {} ({}%)", result.getKoreanLabel(),
                        String.format("%.1f", result.getTopScore() * 100));

            } else {
                result.setSuccess(false);
                result.setMessage("API 호출 실패: " + response.getStatusCode());
                log.error("Hugging Face API 오류: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("음식 인식 중 오류 발생", e);
            result.setSuccess(false);

            // 에러 메시지 분석
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("loading")) {
                result.setMessage("AI 모델이 로딩 중입니다. 20-30초 후 다시 시도해주세요.");
            } else if (errorMsg != null && errorMsg.contains("timeout")) {
                result.setMessage("요청 시간이 초과되었습니다. 다시 시도해주세요.");
            } else {
                result.setMessage("인식 중 오류가 발생했습니다: " + errorMsg);
            }
        }

        return result;
    }

    /**
     * 인식 결과를 기반으로 추천 식재료 목록 반환
     */
    public List<String> getSuggestedIngredients(String label) {
        return LABEL_SUGGESTIONS.getOrDefault(label, Collections.emptyList());
    }

    /**
     * 지원하는 모든 카테고리 목록 반환
     */
    public Map<String, String> getAllCategories() {
        return Collections.unmodifiableMap(LABEL_KOREAN_MAP);
    }
}
