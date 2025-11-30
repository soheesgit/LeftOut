package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final IngredientRepository ingredientRepository;

    /**
     * 기간별 폐기 통계 조회
     * @param userId 사용자 ID
     * @param period 기간 단위 (day, week, month)
     * @param limit 조회 기간 (7일, 4주, 12개월 등)
     * @return 날짜별 폐기 횟수
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDiscardStatsByPeriod(Long userId, String period, int limit) {
        return ingredientRepository.getDiscardStatsByPeriod(userId, period, limit);
    }

    /**
     * 카테고리별 폐기 통계 조회
     * @param userId 사용자 ID
     * @param period 기간 단위 (day, week, month)
     * @param limit 조회 기간
     * @return 카테고리별 폐기 횟수
     */
    public List<Map<String, Object>> getDiscardStatsByCategory(Long userId, String period, int limit) {
        List<Map<String, Object>> stats = ingredientRepository.getDiscardStatsByCategory(userId, period, limit);

        // 카테고리 이름이 null인 경우 "기타"로 처리
        for (Map<String, Object> stat : stats) {
            if (stat.get("categoryName") == null) {
                stat.put("categoryName", "기타");
            }
        }

        return stats;
    }

    /**
     * 월별 소비/폐기 통계 조회
     * @param userId 사용자 ID
     * @param year 연도
     * @param month 월
     * @return 소비/폐기 횟수
     */
    public Map<String, Object> getMonthlyConsumptionStats(Long userId, int year, int month) {
        Map<String, Object> currentMonthStats = ingredientRepository.getMonthlyConsumptionStats(userId, year, month);

        // 전월 통계 조회
        YearMonth currentMonth = YearMonth.of(year, month);
        YearMonth previousMonth = currentMonth.minusMonths(1);
        Map<String, Object> previousMonthStats = ingredientRepository.getMonthlyConsumptionStats(
            userId, previousMonth.getYear(), previousMonth.getMonthValue()
        );

        // 결과 맵 생성
        Map<String, Object> result = new HashMap<>();
        result.put("currentMonth", currentMonthStats);
        result.put("previousMonth", previousMonthStats);

        // 전월 대비 변화율 계산
        long currentConsumed = ((Number) currentMonthStats.get("consumedCount")).longValue();
        long previousConsumed = ((Number) previousMonthStats.get("consumedCount")).longValue();
        long currentDiscarded = ((Number) currentMonthStats.get("discardedCount")).longValue();
        long previousDiscarded = ((Number) previousMonthStats.get("discardedCount")).longValue();

        Map<String, Object> changes = new HashMap<>();
        changes.put("consumedChange", currentConsumed - previousConsumed);
        changes.put("discardedChange", currentDiscarded - previousDiscarded);

        // 변화율 퍼센트 계산
        if (previousConsumed > 0) {
            changes.put("consumedChangePercent",
                Math.round((double)(currentConsumed - previousConsumed) / previousConsumed * 100));
        } else {
            changes.put("consumedChangePercent", currentConsumed > 0 ? 100 : 0);
        }

        if (previousDiscarded > 0) {
            changes.put("discardedChangePercent",
                Math.round((double)(currentDiscarded - previousDiscarded) / previousDiscarded * 100));
        } else {
            changes.put("discardedChangePercent", currentDiscarded > 0 ? 100 : 0);
        }

        result.put("changes", changes);

        return result;
    }

    /**
     * 보관 위치별 카테고리 분포 조회
     * @param userId 사용자 ID
     * @return 보관 위치별 카테고리 분포 (각 위치에서 가장 많은 카테고리)
     */
    public Map<String, Map<String, Object>> getStorageLocationDistribution(Long userId) {
        List<Map<String, Object>> rawData = ingredientRepository.getStorageLocationDistribution(userId);

        // 보관 위치별로 그룹화
        Map<String, List<Map<String, Object>>> groupedByLocation = new HashMap<>();
        for (Map<String, Object> item : rawData) {
            String location = (String) item.get("storageLocation");
            groupedByLocation.computeIfAbsent(location, k -> new ArrayList<>()).add(item);
        }

        // 각 위치별로 가장 많은 카테고리와 전체 분포 계산
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByLocation.entrySet()) {
            String location = entry.getKey();
            List<Map<String, Object>> categories = entry.getValue();

            long totalCount = categories.stream()
                .mapToLong(c -> ((Number) c.get("count")).longValue())
                .sum();

            // 카테고리별 비율 계산
            List<Map<String, Object>> categoryData = new ArrayList<>();
            for (Map<String, Object> category : categories) {
                Map<String, Object> catInfo = new HashMap<>();
                String categoryName = (String) category.get("categoryName");
                long count = ((Number) category.get("count")).longValue();

                catInfo.put("categoryName", categoryName != null ? categoryName : "기타");
                catInfo.put("count", count);
                // 0으로 나누기 방지
                long percentage = totalCount > 0 ? Math.round((double) count / totalCount * 100) : 0;
                catInfo.put("percentage", percentage);
                categoryData.add(catInfo);
            }

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("total", totalCount);
            locationData.put("categories", categoryData);
            locationData.put("topCategory", categoryData.get(0).get("categoryName")); // 가장 많은 카테고리

            result.put(location, locationData);
        }

        return result;
    }

    /**
     * 현재 카테고리 구성 조회 (active 식재료만)
     * @param userId 사용자 ID
     * @return 카테고리별 개수 및 비율
     */
    public List<Map<String, Object>> getCategoryComposition(Long userId) {
        List<Map<String, Object>> rawData = ingredientRepository.getCategoryComposition(userId);

        // 전체 개수 계산
        long totalCount = rawData.stream()
            .mapToLong(c -> ((Number) c.get("count")).longValue())
            .sum();

        // 비율 추가
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> category : rawData) {
            Map<String, Object> catInfo = new HashMap<>(category);
            long count = ((Number) category.get("count")).longValue();
            String categoryName = (String) category.get("categoryName");

            catInfo.put("categoryName", categoryName != null ? categoryName : "기타");
            catInfo.put("count", count);
            catInfo.put("percentage", Math.round((double) count / totalCount * 100));
            result.add(catInfo);
        }

        return result;
    }

    /**
     * 대시보드용 통합 통계 조회
     * @param userId 사용자 ID
     * @param period 기간 단위 (day, week, month)
     * @param limit 조회 기간
     * @return 모든 통계 데이터
     */
    public Map<String, Object> getDashboardStats(Long userId, String period, int limit) {
        Map<String, Object> dashboard = new HashMap<>();

        // 현재 날짜 기준으로 월별 통계
        LocalDate now = LocalDate.now();

        dashboard.put("discardByPeriod", getDiscardStatsByPeriod(userId, period, limit));
        dashboard.put("discardByCategory", getDiscardStatsByCategory(userId, period, limit));
        dashboard.put("monthlyStats", getMonthlyConsumptionStats(userId, now.getYear(), now.getMonthValue()));
        dashboard.put("storageDistribution", getStorageLocationDistribution(userId));
        dashboard.put("categoryComposition", getCategoryComposition(userId));

        return dashboard;
    }
}
