package com.englishlearning.service;

import com.englishlearning.dto.nutrition.*;
import com.englishlearning.entity.nutrition.NutritionActivity;
import com.englishlearning.entity.nutrition.NutritionActivityLog;
import com.englishlearning.entity.nutrition.NutritionDailyLog;
import com.englishlearning.repository.nutrition.NutritionActivityLogRepository;
import com.englishlearning.repository.nutrition.NutritionActivityRepository;
import com.englishlearning.repository.nutrition.NutritionDailyLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class CalorieCalculationService {

    private final NutritionActivityRepository nutritionActivityRepository;
    private final NutritionActivityLogRepository nutritionActivityLogRepository;
    private final NutritionDailyLogRepository nutritionDailyLogRepository;
    private final NutritionCalculationService nutritionCalculationService;

    @PostConstruct
    @Transactional
    public void initDefaultActivities() {
        if (nutritionActivityRepository.count() == 0) {
            log.info("Seeding 2024 Compendium of Physical Activities to NUTRITION_ACTIVITY table...");
            List<NutritionActivity> defaults = List.of(
                    // Cardio
                    NutritionActivity.builder()
                            .name("Nhảy dây")
                            .category("CARDIO")
                            .intensity("MODERATE")
                            .metValue(new BigDecimal("11.0"))
                            .description("Nhảy dây tốc độ trung bình - nhanh (100-120 nhịp/phút)")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Tabata / HIIT")
                            .category("CARDIO")
                            .intensity("HIGH")
                            .metValue(new BigDecimal("11.0"))
                            .description("Luyện tập ngắt quãng cường độ cao (HIIT/Tabata)")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Đạp xe vừa (16-20 km/h)")
                            .category("CARDIO")
                            .intensity("MODERATE")
                            .metValue(new BigDecimal("6.8"))
                            .description("Đạp xe ngoài trời hoặc xe đạp tập tốc độ trung bình")
                            .isActive(true)
                            .build(),

                    // Strength
                    NutritionActivity.builder()
                            .name("Tập tạ - nhiều bài")
                            .category("STRENGTH")
                            .intensity("MODERATE")
                            .metValue(new BigDecimal("3.5"))
                            .description("Tập gym/kháng lực các nhóm cơ tiêu chuẩn, nghỉ vừa")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Tập tạ - cường độ cao")
                            .category("STRENGTH")
                            .intensity("HIGH")
                            .metValue(new BigDecimal("6.0"))
                            .description("Tập tạ nặng, nghỉ ngắn, bài tập phức hợp/superset")
                            .isActive(true)
                            .build(),

                    // Calisthenics
                    NutritionActivity.builder()
                            .name("Chống đẩy")
                            .category("CALISTHENICS")
                            .intensity("HIGH")
                            .metValue(new BigDecimal("7.5"))
                            .description("Chống đẩy (Push-ups) liên tục, nỗ lực cao")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Kéo xà")
                            .category("CALISTHENICS")
                            .intensity("HIGH")
                            .metValue(new BigDecimal("7.5"))
                            .description("Kéo xà đơn (Pull-ups / Chin-ups)")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Tập bụng nhẹ")
                            .category("CALISTHENICS")
                            .intensity("LOW")
                            .metValue(new BigDecimal("2.8"))
                            .description("Gập bụng, plank ngắt quãng, duỗi cơ bụng")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Calisthenics tổng hợp")
                            .category("CALISTHENICS")
                            .intensity("MODERATE")
                            .metValue(new BigDecimal("3.8"))
                            .description("Tập thể hình trọng lượng cơ thể tổng hợp")
                            .isActive(true)
                            .build(),

                    // Sports
                    NutritionActivity.builder()
                            .name("Cầu lông")
                            .category("SPORTS")
                            .intensity("MODERATE")
                            .metValue(new BigDecimal("5.5"))
                            .description("Đánh cầu lông giải trí, đối kháng phong trào")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Cầu lông thi đấu")
                            .category("SPORTS")
                            .intensity("HIGH")
                            .metValue(new BigDecimal("7.0"))
                            .description("Đánh cầu lông thi đấu, cường độ cao, di chuyển liên tục")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Bơi lội tự do")
                            .category("SPORTS")
                            .intensity("MODERATE")
                            .metValue(new BigDecimal("6.0"))
                            .description("Bơi tự do/bơi ếch tốc độ vừa phải")
                            .isActive(true)
                            .build(),

                    // Walking & Running (Pace & Speed based on 2024 Compendium)
                    NutritionActivity.builder()
                            .name("Đi bộ vừa (4.5 km/h)")
                            .category("WALKING")
                            .intensity("MODERATE")
                            .metValue(new BigDecimal("3.5"))
                            .description("Đi bộ đều trên đường bằng")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Đi bộ nhanh (5.5 km/h)")
                            .category("WALKING")
                            .intensity("MODERATE")
                            .metValue(new BigDecimal("4.3"))
                            .description("Đi bộ nhanh, nhịp tim tăng nhẹ")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Đi bộ dốc / leo cầu thang")
                            .category("WALKING")
                            .intensity("HIGH")
                            .metValue(new BigDecimal("6.0"))
                            .description("Đi bộ đường dốc hoặc leo cầu thang bộ liên tục")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Chạy bộ chậm (8.0 km/h - Pace 7:30)")
                            .category("RUNNING")
                            .intensity("HIGH")
                            .metValue(new BigDecimal("8.3"))
                            .description("Chạy bền nhẹ nhàng (Jogging)")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Chạy bộ vừa (10.0 km/h - Pace 6:00)")
                            .category("RUNNING")
                            .intensity("HIGH")
                            .metValue(new BigDecimal("9.8"))
                            .description("Chạy tốc độ trung bình 10 km/h")
                            .isActive(true)
                            .build(),
                    NutritionActivity.builder()
                            .name("Chạy bộ nhanh (12.0 km/h - Pace 5:00)")
                            .category("RUNNING")
                            .intensity("HIGH")
                            .metValue(new BigDecimal("11.5"))
                            .description("Chạy tốc độ cao 12 km/h")
                            .isActive(true)
                            .build()
            );

            nutritionActivityRepository.saveAll(defaults);
            log.info("Successfully seeded {} physical activities.", defaults.size());
        }
    }

    @Transactional(readOnly = true)
    public List<NutritionActivityDto> getActiveActivities() {
        List<NutritionActivity> list = nutritionActivityRepository.findAllByIsActiveTrueOrderByCategoryAscNameAsc();
        return list.stream().map(this::mapToActivityDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NutritionActivityLogResponse> getActivityLogs(Long userId, LocalDate logDate) {
        LocalDate targetDate = logDate != null ? logDate : LocalDate.now();
        List<NutritionActivityLog> logs = nutritionActivityLogRepository.findByUserIdAndLogDate(userId, targetDate);
        return logs.stream().map(this::mapToLogResponse).collect(Collectors.toList());
    }

    @Transactional
    public NutritionActivityLogResponse addActivityLog(Long userId, NutritionActivityLogRequest request) {
        if (request.getActivityId() == null) {
            throw new IllegalArgumentException("Hoạt động không được để trống");
        }
        if (request.getDurationMinutes() == null || request.getDurationMinutes().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Thời gian tập phải lớn hơn 0 phút");
        }

        NutritionActivity activity = nutritionActivityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động với ID: " + request.getActivityId()));

        BigDecimal weight = request.getWeightKg() != null && request.getWeightKg().compareTo(BigDecimal.ZERO) > 0
                ? request.getWeightKg() : new BigDecimal("68.0");

        // Net calories calculation
        BigDecimal netCalories = nutritionCalculationService.calculateNetActivityCalories(
                activity.getMetValue(), weight, request.getDurationMinutes());

        NutritionActivityLog logEntity = NutritionActivityLog.builder()
                .userId(userId)
                .logDate(request.getLogDate() != null ? request.getLogDate() : LocalDate.now())
                .activity(activity)
                .durationMinutes(request.getDurationMinutes())
                .weightKg(weight)
                .metValue(activity.getMetValue()) // Snapshot MET
                .caloriesBurned(netCalories)      // Snapshot NET calories
                .build();

        NutritionActivityLog saved = nutritionActivityLogRepository.save(logEntity);
        return mapToLogResponse(saved);
    }

    @Transactional
    public NutritionActivityLogResponse updateActivityLog(Long id, Long userId, NutritionActivityLogRequest request) {
        NutritionActivityLog existing = nutritionActivityLogRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi hoạt động với ID: " + id));

        if (request.getActivityId() != null) {
            NutritionActivity activity = nutritionActivityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động với ID: " + request.getActivityId()));
            existing.setActivity(activity);
            existing.setMetValue(activity.getMetValue());
        }

        if (request.getDurationMinutes() != null) {
            if (request.getDurationMinutes().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Thời gian tập phải lớn hơn 0");
            }
            existing.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getWeightKg() != null && request.getWeightKg().compareTo(BigDecimal.ZERO) > 0) {
            existing.setWeightKg(request.getWeightKg());
        }

        if (request.getLogDate() != null) {
            existing.setLogDate(request.getLogDate());
        }

        // Recalculate net calories for updated log
        BigDecimal netCalories = nutritionCalculationService.calculateNetActivityCalories(
                existing.getMetValue(), existing.getWeightKg(), existing.getDurationMinutes());
        existing.setCaloriesBurned(netCalories);

        NutritionActivityLog updated = nutritionActivityLogRepository.save(existing);
        return mapToLogResponse(updated);
    }

    @Transactional
    public void deleteActivityLog(Long id, Long userId) {
        NutritionActivityLog logEntity = nutritionActivityLogRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi hoạt động với ID: " + id));
        nutritionActivityLogRepository.delete(logEntity);
    }

    @Transactional(readOnly = true)
    public CalorieDeficitSummaryResponse getCalorieDeficitSummary(Long userId, LocalDate date, BigDecimal customTdee, BigDecimal customWeight) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        BigDecimal tdee = (customTdee != null && customTdee.compareTo(BigDecimal.ZERO) > 0) ? customTdee : new BigDecimal("2100");
        BigDecimal weight = (customWeight != null && customWeight.compareTo(BigDecimal.ZERO) > 0) ? customWeight : new BigDecimal("68.0");

        // 1. Calories consumed from food (NUTRITION_DAILY_LOG)
        List<NutritionDailyLog> foodLogs = nutritionDailyLogRepository.getLogsByDate(userId, targetDate);
        BigDecimal foodCalories = BigDecimal.ZERO;
        for (NutritionDailyLog fLog : foodLogs) {
            if (fLog.getCalories() != null) {
                foodCalories = foodCalories.add(fLog.getCalories());
            }
        }
        foodCalories = foodCalories.setScale(1, RoundingMode.HALF_UP);

        // 2. Activities for the date (NUTRITION_ACTIVITY_LOG)
        List<NutritionActivityLog> activityLogs = nutritionActivityLogRepository.findByUserIdAndLogDate(userId, targetDate);
        BigDecimal activityCalories = BigDecimal.ZERO;
        List<NutritionActivityLogResponse> logResponses = new ArrayList<>();

        for (NutritionActivityLog aLog : activityLogs) {
            logResponses.add(mapToLogResponse(aLog));
            if (aLog.getCaloriesBurned() != null) {
                activityCalories = activityCalories.add(aLog.getCaloriesBurned());
            }
        }
        activityCalories = activityCalories.setScale(1, RoundingMode.HALF_UP);

        // 3. Total Calories Burned = TDEE + Total Net Activity Calories
        BigDecimal totalBurned = tdee.add(activityCalories).setScale(1, RoundingMode.HALF_UP);

        // 4. Calorie Balance = Total Calories Burned - Calories Consumed
        BigDecimal balance = totalBurned.subtract(foodCalories).setScale(1, RoundingMode.HALF_UP);

        // 5. Status: DEFICIT (> 0), SURPLUS (< 0), MAINTENANCE (== 0)
        String status;
        int cmp = balance.compareTo(BigDecimal.ZERO);
        if (cmp > 0) {
            status = "DEFICIT";
        } else if (cmp < 0) {
            status = "SURPLUS";
        } else {
            status = "MAINTENANCE";
        }

        return CalorieDeficitSummaryResponse.builder()
                .logDate(targetDate)
                .weightKg(weight)
                .tdee(tdee)
                .foodCalories(foodCalories)
                .activityCalories(activityCalories)
                .totalCaloriesBurned(totalBurned)
                .calorieBalance(balance)
                .status(status)
                .activityLogs(logResponses)
                .loggedFoodsCount(foodLogs.size())
                .build();
    }

    @Transactional(readOnly = true)
    public CalorieDeficitSummaryResponse calculateDeficitPreview(Long userId, CalorieDeficitCalculationRequest request) {
        LocalDate targetDate = request.getLogDate() != null ? request.getLogDate() : LocalDate.now();
        BigDecimal tdee = (request.getTdee() != null && request.getTdee().compareTo(BigDecimal.ZERO) > 0) ? request.getTdee() : new BigDecimal("2100");
        BigDecimal weight = (request.getWeightKg() != null && request.getWeightKg().compareTo(BigDecimal.ZERO) > 0) ? request.getWeightKg() : new BigDecimal("68.0");

        // Food calories: use request.foodCalories if provided, otherwise query from daily logs
        BigDecimal foodCalories = BigDecimal.ZERO;
        int loggedFoodsCount = 0;
        if (request.getFoodCalories() != null) {
            foodCalories = request.getFoodCalories();
        } else {
            List<NutritionDailyLog> foodLogs = nutritionDailyLogRepository.getLogsByDate(userId, targetDate);
            loggedFoodsCount = foodLogs.size();
            for (NutritionDailyLog fLog : foodLogs) {
                if (fLog.getCalories() != null) {
                    foodCalories = foodCalories.add(fLog.getCalories());
                }
            }
        }
        foodCalories = foodCalories.setScale(1, RoundingMode.HALF_UP);


        // Calculate activity calories from request items
        BigDecimal activityCalories = BigDecimal.ZERO;
        List<NutritionActivityLogResponse> previewLogs = new ArrayList<>();

        if (request.getActivities() != null) {
            for (CalorieDeficitCalculationRequest.Item item : request.getActivities()) {
                if (item.getActivityId() != null && item.getDurationMinutes() != null && item.getDurationMinutes().compareTo(BigDecimal.ZERO) > 0) {
                    NutritionActivity activity = nutritionActivityRepository.findById(item.getActivityId()).orElse(null);
                    if (activity != null) {
                        BigDecimal netCal = nutritionCalculationService.calculateNetActivityCalories(activity.getMetValue(), weight, item.getDurationMinutes());
                        activityCalories = activityCalories.add(netCal);

                        previewLogs.add(NutritionActivityLogResponse.builder()
                                .activityId(activity.getId())
                                .activityName(activity.getName())
                                .category(activity.getCategory())
                                .intensity(activity.getIntensity())
                                .durationMinutes(item.getDurationMinutes())
                                .weightKg(weight)
                                .metValue(activity.getMetValue())
                                .caloriesBurned(netCal)
                                .logDate(targetDate)
                                .build());
                    }
                }
            }
        }
        activityCalories = activityCalories.setScale(1, RoundingMode.HALF_UP);

        BigDecimal totalBurned = tdee.add(activityCalories).setScale(1, RoundingMode.HALF_UP);
        BigDecimal balance = totalBurned.subtract(foodCalories).setScale(1, RoundingMode.HALF_UP);

        String status;
        int cmp = balance.compareTo(BigDecimal.ZERO);
        if (cmp > 0) {
            status = "DEFICIT";
        } else if (cmp < 0) {
            status = "SURPLUS";
        } else {
            status = "MAINTENANCE";
        }

        return CalorieDeficitSummaryResponse.builder()
                .logDate(targetDate)
                .weightKg(weight)
                .tdee(tdee)
                .foodCalories(foodCalories)
                .activityCalories(activityCalories)
                .totalCaloriesBurned(totalBurned)
                .calorieBalance(balance)
                .status(status)
                .activityLogs(previewLogs)
                .loggedFoodsCount(loggedFoodsCount)
                .build();

    }

    private NutritionActivityDto mapToActivityDto(NutritionActivity entity) {
        return NutritionActivityDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .intensity(entity.getIntensity())
                .metValue(entity.getMetValue())
                .description(entity.getDescription())
                .build();
    }

    private NutritionActivityLogResponse mapToLogResponse(NutritionActivityLog entity) {
        return NutritionActivityLogResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .logDate(entity.getLogDate())
                .activityId(entity.getActivity() != null ? entity.getActivity().getId() : null)
                .activityName(entity.getActivity() != null ? entity.getActivity().getName() : "Hoạt động")
                .category(entity.getActivity() != null ? entity.getActivity().getCategory() : "GENERAL")
                .intensity(entity.getActivity() != null ? entity.getActivity().getIntensity() : "MODERATE")
                .durationMinutes(entity.getDurationMinutes())
                .weightKg(entity.getWeightKg())
                .metValue(entity.getMetValue())
                .caloriesBurned(entity.getCaloriesBurned())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public NutritionStatisticsResponse getNutritionStatistics(
            Long userId,
            String range,
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal tdeeParam,
            BigDecimal weightParam) {

        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today;

        if (fromDate != null && toDate != null) {
            startDate = fromDate;
            endDate = toDate;
        } else if ("14_DAYS".equalsIgnoreCase(range)) {
            startDate = today.minusDays(13);
        } else if ("30_DAYS".equalsIgnoreCase(range)) {
            startDate = today.minusDays(29);
        } else if ("THIS_MONTH".equalsIgnoreCase(range)) {
            startDate = today.withDayOfMonth(1);
            endDate = today.withDayOfMonth(today.lengthOfMonth());
        } else { // Default 7_DAYS
            startDate = today.minusDays(6);
        }

        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        BigDecimal tdee = (tdeeParam != null && tdeeParam.compareTo(BigDecimal.ZERO) > 0)
                ? tdeeParam : new BigDecimal("2100");
        BigDecimal weight = (weightParam != null && weightParam.compareTo(BigDecimal.ZERO) > 0)
                ? weightParam : new BigDecimal("68.0");

        // Fetch logs in date range
        List<NutritionDailyLog> foodLogs = nutritionDailyLogRepository.findByUserIdAndLogDateBetween(userId, startDate, endDate);
        List<NutritionActivityLog> activityLogs = nutritionActivityLogRepository.findByUserIdAndLogDateBetween(userId, startDate, endDate);

        // Map logs by date
        Map<LocalDate, List<NutritionDailyLog>> foodByDate = foodLogs.stream()
                .collect(Collectors.groupingBy(NutritionDailyLog::getLogDate));
        Map<LocalDate, List<NutritionActivityLog>> activityByDate = activityLogs.stream()
                .collect(Collectors.groupingBy(NutritionActivityLog::getLogDate));

        List<NutritionStatisticsResponse.DailyTrendItem> dailyTrends = new ArrayList<>();

        BigDecimal totalFoodCalories = BigDecimal.ZERO;
        BigDecimal totalActivityCalories = BigDecimal.ZERO;
        BigDecimal totalBurnedCalories = BigDecimal.ZERO;
        BigDecimal netCalorieBalance = BigDecimal.ZERO;
        BigDecimal totalWorkoutMinutes = BigDecimal.ZERO;
        int totalWorkoutSessions = activityLogs.size();

        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarbohydrate = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;

        int deficitDaysCount = 0;
        int surplusDaysCount = 0;
        int maintenanceDaysCount = 0;
        int loggedDaysCount = 0;
        int gymDaysCount = 0;
        int nonGymWorkoutDaysCount = 0;
        int restDaysCount = 0;

        // Iterate through each date in the range
        LocalDate cur = startDate;
        int daysCount = 0;
        while (!cur.isAfter(endDate)) {
            daysCount++;
            List<NutritionDailyLog> dayFoods = foodByDate.getOrDefault(cur, Collections.emptyList());
            List<NutritionActivityLog> dayActivities = activityByDate.getOrDefault(cur, Collections.emptyList());

            BigDecimal dayFoodCal = BigDecimal.ZERO;
            BigDecimal dayProtein = BigDecimal.ZERO;
            BigDecimal dayCarb = BigDecimal.ZERO;
            BigDecimal dayFat = BigDecimal.ZERO;
            BigDecimal dayFiber = BigDecimal.ZERO;

            for (NutritionDailyLog f : dayFoods) {
                if (f.getCalories() != null) dayFoodCal = dayFoodCal.add(f.getCalories());
                if (f.getProtein() != null) dayProtein = dayProtein.add(f.getProtein());
                if (f.getCarbohydrate() != null) dayCarb = dayCarb.add(f.getCarbohydrate());
                if (f.getFat() != null) dayFat = dayFat.add(f.getFat());
                if (f.getFiber() != null) dayFiber = dayFiber.add(f.getFiber());
            }

            BigDecimal dayActCal = BigDecimal.ZERO;
            BigDecimal dayWorkoutMins = BigDecimal.ZERO;
            boolean hasGym = false;
            List<String> actSummaries = new ArrayList<>();

            for (NutritionActivityLog a : dayActivities) {
                if (a.getCaloriesBurned() != null) dayActCal = dayActCal.add(a.getCaloriesBurned());
                if (a.getDurationMinutes() != null) dayWorkoutMins = dayWorkoutMins.add(a.getDurationMinutes());
                if (a.getActivity() != null) {
                    String cat = a.getActivity().getCategory();
                    String name = a.getActivity().getName();
                    if ("STRENGTH".equalsIgnoreCase(cat) || (name != null && (name.toLowerCase().contains("tạ") || name.toLowerCase().contains("gym")))) {
                        hasGym = true;
                    }
                    int mins = a.getDurationMinutes() != null ? a.getDurationMinutes().intValue() : 0;
                    actSummaries.add(name + (mins > 0 ? " (" + mins + "p)" : ""));
                }
            }

            if (hasGym) {
                gymDaysCount++;
            } else if (!dayActivities.isEmpty()) {
                nonGymWorkoutDaysCount++;
            } else {
                restDaysCount++;
            }

            String activitySummary = String.join(", ", actSummaries);

            boolean hasData = !dayFoods.isEmpty() || !dayActivities.isEmpty();
            BigDecimal dayTotalBurned;
            BigDecimal dayBalance;
            String status;

            if (hasData) {
                loggedDaysCount++;
                dayTotalBurned = tdee.add(dayActCal).setScale(1, RoundingMode.HALF_UP);
                dayBalance = dayTotalBurned.subtract(dayFoodCal).setScale(1, RoundingMode.HALF_UP);

                int cmp = dayBalance.compareTo(BigDecimal.ZERO);
                if (cmp > 0) {
                    status = "DEFICIT";
                    deficitDaysCount++;
                } else if (cmp < 0) {
                    status = "SURPLUS";
                    surplusDaysCount++;
                } else {
                    status = "MAINTENANCE";
                    maintenanceDaysCount++;
                }

                totalBurnedCalories = totalBurnedCalories.add(dayTotalBurned);
                netCalorieBalance = netCalorieBalance.add(dayBalance);
            } else {
                dayTotalBurned = BigDecimal.ZERO;
                dayBalance = BigDecimal.ZERO;
                status = "NO_DATA";
            }

            // Day of week in Vietnamese
            String dayOfWeek = formatDayOfWeek(cur.getDayOfWeek().getValue());

            dailyTrends.add(NutritionStatisticsResponse.DailyTrendItem.builder()
                    .date(cur)
                    .dayOfWeek(dayOfWeek)
                    .foodCalories(dayFoodCal.setScale(1, RoundingMode.HALF_UP))
                    .activityCalories(dayActCal.setScale(1, RoundingMode.HALF_UP))
                    .totalBurned(dayTotalBurned)
                    .tdee(tdee)
                    .calorieBalance(dayBalance)
                    .status(status)
                    .protein(dayProtein.setScale(1, RoundingMode.HALF_UP))
                    .carbohydrate(dayCarb.setScale(1, RoundingMode.HALF_UP))
                    .fat(dayFat.setScale(1, RoundingMode.HALF_UP))
                    .workoutMinutes(dayWorkoutMins.setScale(1, RoundingMode.HALF_UP))
                    .foodCount(dayFoods.size())
                    .activityCount(dayActivities.size())
                    .hasGym(hasGym)
                    .activitySummary(activitySummary)
                    .build());

            totalFoodCalories = totalFoodCalories.add(dayFoodCal);
            totalActivityCalories = totalActivityCalories.add(dayActCal);
            totalWorkoutMinutes = totalWorkoutMinutes.add(dayWorkoutMins);

            totalProtein = totalProtein.add(dayProtein);
            totalCarbohydrate = totalCarbohydrate.add(dayCarb);
            totalFat = totalFat.add(dayFat);
            totalFiber = totalFiber.add(dayFiber);

            cur = cur.plusDays(1);
        }

        BigDecimal loggedDaysBD = loggedDaysCount > 0 ? new BigDecimal(loggedDaysCount) : BigDecimal.ONE;
        BigDecimal avgDailyFood = loggedDaysCount > 0 ? totalFoodCalories.divide(loggedDaysBD, 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgDailyActivity = loggedDaysCount > 0 ? totalActivityCalories.divide(loggedDaysBD, 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgDailyBurned = loggedDaysCount > 0 ? totalBurnedCalories.divide(loggedDaysBD, 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgDailyBalance = loggedDaysCount > 0 ? netCalorieBalance.divide(loggedDaysBD, 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgDailyWorkout = loggedDaysCount > 0 ? totalWorkoutMinutes.divide(loggedDaysBD, 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal avgProtein = loggedDaysCount > 0 ? totalProtein.divide(loggedDaysBD, 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgCarb = loggedDaysCount > 0 ? totalCarbohydrate.divide(loggedDaysBD, 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgFat = loggedDaysCount > 0 ? totalFat.divide(loggedDaysBD, 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal deficitRate = loggedDaysCount > 0
                ? new BigDecimal(deficitDaysCount).multiply(new BigDecimal("100")).divide(loggedDaysBD, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 1 kg of fat ~= 7700 kcal deficit
        BigDecimal estimatedFatChange = loggedDaysCount > 0
                ? netCalorieBalance.divide(new BigDecimal("7700"), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Category breakdown
        Map<String, List<NutritionActivityLog>> categoryMap = activityLogs.stream()
                .collect(Collectors.groupingBy(l -> l.getActivity() != null ? l.getActivity().getCategory() : "GENERAL"));

        List<NutritionStatisticsResponse.CategorySummary> categorySummaries = new ArrayList<>();
        for (Map.Entry<String, List<NutritionActivityLog>> entry : categoryMap.entrySet()) {
            String cat = entry.getKey();
            List<NutritionActivityLog> catLogs = entry.getValue();

            BigDecimal catMinutes = catLogs.stream()
                    .map(l -> l.getDurationMinutes() != null ? l.getDurationMinutes() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal catCalories = catLogs.stream()
                    .map(l -> l.getCaloriesBurned() != null ? l.getCaloriesBurned() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal percentage = totalActivityCalories.compareTo(BigDecimal.ZERO) > 0
                    ? catCalories.multiply(new BigDecimal("100")).divide(totalActivityCalories, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            categorySummaries.add(NutritionStatisticsResponse.CategorySummary.builder()
                    .category(cat)
                    .categoryName(formatCategoryName(cat))
                    .totalMinutes(catMinutes.setScale(1, RoundingMode.HALF_UP))
                    .totalCalories(catCalories.setScale(1, RoundingMode.HALF_UP))
                    .sessionsCount(catLogs.size())
                    .percentage(percentage)
                    .build());
        }

        return NutritionStatisticsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .daysCount(daysCount)
                .loggedDaysCount(loggedDaysCount)
                .tdee(tdee)
                .weightKg(weight)
                .totalFoodCalories(totalFoodCalories.setScale(1, RoundingMode.HALF_UP))
                .avgDailyFoodCalories(avgDailyFood)
                .totalActivityCalories(totalActivityCalories.setScale(1, RoundingMode.HALF_UP))
                .avgDailyActivityCalories(avgDailyActivity)
                .totalBurnedCalories(totalBurnedCalories.setScale(1, RoundingMode.HALF_UP))
                .avgDailyBurnedCalories(avgDailyBurned)
                .netCalorieBalance(netCalorieBalance.setScale(1, RoundingMode.HALF_UP))
                .avgDailyCalorieBalance(avgDailyBalance)
                .estimatedFatKgChange(estimatedFatChange)
                .totalWorkoutMinutes(totalWorkoutMinutes.setScale(1, RoundingMode.HALF_UP))
                .avgDailyWorkoutMinutes(avgDailyWorkout)
                .workoutSessionsCount(totalWorkoutSessions)
                .deficitDaysCount(deficitDaysCount)
                .surplusDaysCount(surplusDaysCount)
                .maintenanceDaysCount(maintenanceDaysCount)
                .deficitRatePercent(deficitRate)
                .gymDaysCount(gymDaysCount)
                .nonGymWorkoutDaysCount(nonGymWorkoutDaysCount)
                .restDaysCount(restDaysCount)
                .totalProtein(totalProtein.setScale(1, RoundingMode.HALF_UP))
                .totalCarbohydrate(totalCarbohydrate.setScale(1, RoundingMode.HALF_UP))
                .totalFat(totalFat.setScale(1, RoundingMode.HALF_UP))
                .totalFiber(totalFiber.setScale(1, RoundingMode.HALF_UP))
                .avgDailyProtein(avgProtein)
                .avgDailyCarbohydrate(avgCarb)
                .avgDailyFat(avgFat)
                .categoryBreakdown(categorySummaries)
                .dailyTrend(dailyTrends)
                .build();
    }

    private String formatDayOfWeek(int day) {
        return switch (day) {
            case 1 -> "Thứ 2";
            case 2 -> "Thứ 3";
            case 3 -> "Thứ 4";
            case 4 -> "Thứ 5";
            case 5 -> "Thứ 6";
            case 6 -> "Thứ 7";
            default -> "Chủ nhật";
        };
    }

    private String formatCategoryName(String cat) {
        return switch (cat) {
            case "CARDIO" -> "Cardio & Sức bền";
            case "STRENGTH" -> "Tập tạ (Strength)";
            case "CALISTHENICS" -> "Calisthenics";
            case "SPORTS" -> "Thể thao & Đối kháng";
            default -> "Hoạt động chung";
        };
    }
}

