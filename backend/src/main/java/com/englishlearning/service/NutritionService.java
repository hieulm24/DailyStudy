package com.englishlearning.service;

import com.englishlearning.dto.nutrition.*;

import java.time.LocalDate;
import java.util.List;

public interface NutritionService {

    List<NutritionFoodSearchDto> searchFoods(Long userId, String keyword);

    NutritionFoodSearchDto getFoodDetail(Long foodId);

    NutritionDailySummaryResponse getDailySummary(Long userId, LocalDate date);

    NutritionDailyLogResponse addDailyLog(Long userId, NutritionDailyLogRequest request);

    NutritionDailyLogResponse updateDailyLog(Long id, Long userId, NutritionDailyLogRequest request);

    void deleteDailyLog(Long id, Long userId);

    NutritionDailySummaryResponse copyDayLogs(Long userId, LocalDate fromDate, LocalDate toDate);

    List<NutritionRecentFoodDto> getRecentFoods(Long userId);

    List<NutritionUserFoodDto> getUserFoods(Long userId);

    NutritionUserFoodDto createUserFood(Long userId, NutritionUserFoodRequest request);

    void deleteUserFood(Long id, Long userId);

    void initSampleFoodsIfEmpty();
}
