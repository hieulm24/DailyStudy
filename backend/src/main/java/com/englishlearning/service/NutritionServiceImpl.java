package com.englishlearning.service;

import com.englishlearning.dto.nutrition.*;
import com.englishlearning.entity.nutrition.*;
import com.englishlearning.repository.nutrition.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionServiceImpl implements NutritionService {

    private final NutritionFoodRepository nutritionFoodRepository;
    private final NutritionFoodVariantRepository nutritionFoodVariantRepository;
    private final NutritionMicronutrientRepository nutritionMicronutrientRepository;
    private final NutritionFoodUnitRepository nutritionFoodUnitRepository;
    private final NutritionUserFoodRepository nutritionUserFoodRepository;
    private final NutritionUserFoodMicronutrientRepository nutritionUserFoodMicronutrientRepository;
    private final NutritionDailyLogRepository nutritionDailyLogRepository;
    private final NutritionCalculationService nutritionCalculationService;

    @PostConstruct
    public void init() {
        try {
            initSampleFoodsIfEmpty();
        } catch (Exception e) {
            log.error("Failed to initialize sample nutrition data: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NutritionFoodSearchDto> searchFoods(Long userId, String keyword) {
        String query = (keyword != null) ? keyword.trim() : "";
        List<NutritionFoodSearchDto> results = new ArrayList<>();

        // 1. Search system foods
        List<NutritionFood> systemFoods = query.isEmpty()
                ? nutritionFoodRepository.findAllByIsActiveTrueOrderByNameAsc()
                : nutritionFoodRepository.searchByName(query);

        for (NutritionFood food : systemFoods) {
            results.add(mapToFoodSearchDto(food));
        }

        // 2. Search user custom foods
        if (userId != null) {
            List<NutritionUserFood> userFoods = query.isEmpty()
                    ? nutritionUserFoodRepository.findByUserIdOrderByNameAsc(userId)
                    : nutritionUserFoodRepository.searchByUserIdAndName(userId, query);

            for (NutritionUserFood uFood : userFoods) {
                results.add(mapUserFoodToSearchDto(uFood));
            }
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionFoodSearchDto getFoodDetail(Long foodId) {
        NutritionFood food = nutritionFoodRepository.findById(foodId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thực phẩm với ID: " + foodId));
        return mapToFoodSearchDto(food);
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionDailySummaryResponse getDailySummary(Long userId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<NutritionDailyLog> logs = nutritionDailyLogRepository.getLogsByDate(userId, targetDate);

        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarb = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;

        List<NutritionDailyLogResponse> items = new ArrayList<>();
        Map<String, TopMicronutrientDto> microMap = new LinkedHashMap<>();

        for (NutritionDailyLog log : logs) {
            NutritionDailyLogResponse logResp = mapToDailyLogResponse(log);
            items.add(logResp);

            if (log.getCalories() != null) totalCalories = totalCalories.add(log.getCalories());
            if (log.getProtein() != null) totalProtein = totalProtein.add(log.getProtein());
            if (log.getCarbohydrate() != null) totalCarb = totalCarb.add(log.getCarbohydrate());
            if (log.getFat() != null) totalFat = totalFat.add(log.getFat());
            if (log.getFiber() != null) totalFiber = totalFiber.add(log.getFiber());

            // Aggregate micronutrients
            for (NutritionMicronutrientDto micro : logResp.getMicronutrients()) {
                String key = micro.getNutrientName().trim();
                TopMicronutrientDto existing = microMap.get(key);
                if (existing == null) {
                    microMap.put(key, TopMicronutrientDto.builder()
                            .nutrientName(key)
                            .totalAmount(micro.getAmount() != null ? micro.getAmount() : BigDecimal.ZERO)
                            .unit(micro.getUnit())
                            .build());
                } else {
                    BigDecimal updated = existing.getTotalAmount().add(micro.getAmount() != null ? micro.getAmount() : BigDecimal.ZERO);
                    existing.setTotalAmount(updated.setScale(2, RoundingMode.HALF_UP));
                }
            }
        }

        List<TopMicronutrientDto> topMicros = new ArrayList<>(microMap.values());
        topMicros.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));

        return NutritionDailySummaryResponse.builder()
                .logDate(targetDate)
                .totalCalories(totalCalories.setScale(0, RoundingMode.HALF_UP))
                .totalProtein(totalProtein.setScale(1, RoundingMode.HALF_UP))
                .totalCarbohydrate(totalCarb.setScale(1, RoundingMode.HALF_UP))
                .totalFat(totalFat.setScale(1, RoundingMode.HALF_UP))
                .totalFiber(totalFiber.setScale(1, RoundingMode.HALF_UP))
                .items(items)
                .topMicronutrients(topMicros)
                .build();
    }

    @Override
    @Transactional
    public NutritionDailyLogResponse addDailyLog(Long userId, NutritionDailyLogRequest request) {
        if (request.getFoodVariantId() == null && request.getUserFoodId() == null) {
            throw new IllegalArgumentException("Phải chọn thực phẩm hệ thống hoặc thực phẩm cá nhân");
        }
        if (request.getFoodVariantId() != null && request.getUserFoodId() != null) {
            throw new IllegalArgumentException("Không thể chọn đồng thời thực phẩm hệ thống và thực phẩm cá nhân");
        }

        NutritionDailyLog dailyLog = new NutritionDailyLog();
        dailyLog.setUserId(userId);
        dailyLog.setLogDate(request.getLogDate() != null ? request.getLogDate() : LocalDate.now());
        dailyLog.setQuantity(request.getQuantity());
        dailyLog.setUnit(request.getUnit());

        if (request.getFoodVariantId() != null) {
            NutritionFoodVariant variant = nutritionFoodVariantRepository.findById(request.getFoodVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trạng thái thực phẩm với ID: " + request.getFoodVariantId()));
            dailyLog.setFoodVariant(variant);

            NutritionCalculationService.CalculatedMacroResult res = nutritionCalculationService.calculateForVariant(
                    variant, request.getQuantity(), request.getUnit());
            dailyLog.setCalculatedGrams(res.getCalculatedGrams());
            dailyLog.setCalculatedMl(res.getCalculatedMl());
            dailyLog.setCalories(res.getCalories());
            dailyLog.setProtein(res.getProtein());
            dailyLog.setCarbohydrate(res.getCarbohydrate());
            dailyLog.setFat(res.getFat());
            dailyLog.setFiber(res.getFiber());
        } else {
            NutritionUserFood userFood = nutritionUserFoodRepository.findByIdAndUserId(request.getUserFoodId(), userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thực phẩm cá nhân với ID: " + request.getUserFoodId()));
            dailyLog.setUserFood(userFood);

            NutritionCalculationService.CalculatedMacroResult res = nutritionCalculationService.calculateForUserFood(
                    userFood, request.getQuantity(), request.getUnit());
            dailyLog.setCalculatedGrams(res.getCalculatedGrams());
            dailyLog.setCalculatedMl(res.getCalculatedMl());
            dailyLog.setCalories(res.getCalories());
            dailyLog.setProtein(res.getProtein());
            dailyLog.setCarbohydrate(res.getCarbohydrate());
            dailyLog.setFat(res.getFat());
            dailyLog.setFiber(res.getFiber());
        }

        NutritionDailyLog saved = nutritionDailyLogRepository.save(dailyLog);
        return mapToDailyLogResponse(saved);
    }

    @Override
    @Transactional
    public NutritionDailyLogResponse updateDailyLog(Long id, Long userId, NutritionDailyLogRequest request) {
        NutritionDailyLog dailyLog = nutritionDailyLogRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi nhật ký với ID: " + id));

        dailyLog.setQuantity(request.getQuantity());
        dailyLog.setUnit(request.getUnit());
        if (request.getLogDate() != null) {
            dailyLog.setLogDate(request.getLogDate());
        }

        if (dailyLog.getFoodVariant() != null) {
            NutritionCalculationService.CalculatedMacroResult res = nutritionCalculationService.calculateForVariant(
                    dailyLog.getFoodVariant(), request.getQuantity(), request.getUnit());
            dailyLog.setCalculatedGrams(res.getCalculatedGrams());
            dailyLog.setCalculatedMl(res.getCalculatedMl());
            dailyLog.setCalories(res.getCalories());
            dailyLog.setProtein(res.getProtein());
            dailyLog.setCarbohydrate(res.getCarbohydrate());
            dailyLog.setFat(res.getFat());
            dailyLog.setFiber(res.getFiber());
        } else if (dailyLog.getUserFood() != null) {
            NutritionCalculationService.CalculatedMacroResult res = nutritionCalculationService.calculateForUserFood(
                    dailyLog.getUserFood(), request.getQuantity(), request.getUnit());
            dailyLog.setCalculatedGrams(res.getCalculatedGrams());
            dailyLog.setCalculatedMl(res.getCalculatedMl());
            dailyLog.setCalories(res.getCalories());
            dailyLog.setProtein(res.getProtein());
            dailyLog.setCarbohydrate(res.getCarbohydrate());
            dailyLog.setFat(res.getFat());
            dailyLog.setFiber(res.getFiber());
        }

        NutritionDailyLog saved = nutritionDailyLogRepository.save(dailyLog);
        return mapToDailyLogResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDailyLog(Long id, Long userId) {
        NutritionDailyLog dailyLog = nutritionDailyLogRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi nhật ký với ID: " + id));
        nutritionDailyLogRepository.delete(dailyLog);
    }

    @Override
    @Transactional
    public NutritionDailySummaryResponse copyDayLogs(Long userId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Ngày nguồn và ngày đích không được để trống");
        }

        List<NutritionDailyLog> sourceLogs = nutritionDailyLogRepository.getLogsByDate(userId, fromDate);
        for (NutritionDailyLog src : sourceLogs) {
            NutritionDailyLog copy = NutritionDailyLog.builder()
                    .userId(userId)
                    .logDate(toDate)
                    .foodVariant(src.getFoodVariant())
                    .userFood(src.getUserFood())
                    .quantity(src.getQuantity())
                    .unit(src.getUnit())
                    .calculatedGrams(src.getCalculatedGrams())
                    .calculatedMl(src.getCalculatedMl())
                    .calories(src.getCalories())
                    .protein(src.getProtein())
                    .carbohydrate(src.getCarbohydrate())
                    .fat(src.getFat())
                    .fiber(src.getFiber())
                    .build();
            nutritionDailyLogRepository.save(copy);
        }

        return getDailySummary(userId, toDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NutritionRecentFoodDto> getRecentFoods(Long userId) {
        List<NutritionDailyLog> recentLogs = nutritionDailyLogRepository.findRecentLogsByUserId(userId);
        Map<String, NutritionRecentFoodDto> map = new LinkedHashMap<>();

        for (NutritionDailyLog log : recentLogs) {
            String key;
            if (log.getFoodVariant() != null) {
                key = "VAR_" + log.getFoodVariant().getId();
                if (!map.containsKey(key)) {
                    map.put(key, NutritionRecentFoodDto.builder()
                            .foodVariantId(log.getFoodVariant().getId())
                            .name(log.getFoodVariant().getFood() != null ? log.getFoodVariant().getFood().getName() : "Món ăn")
                            .state(log.getFoodVariant().getState())
                            .defaultQuantity(log.getQuantity())
                            .defaultUnit(log.getUnit())
                            .isUserCustom(false)
                            .build());
                }
            } else if (log.getUserFood() != null) {
                key = "USR_" + log.getUserFood().getId();
                if (!map.containsKey(key)) {
                    map.put(key, NutritionRecentFoodDto.builder()
                            .userFoodId(log.getUserFood().getId())
                            .name(log.getUserFood().getName())
                            .state(log.getUserFood().getState())
                            .defaultQuantity(log.getQuantity())
                            .defaultUnit(log.getUnit())
                            .isUserCustom(true)
                            .build());
                }
            }
            if (map.size() >= 8) {
                break;
            }
        }

        // If no recent items, suggest top defaults from system
        if (map.isEmpty()) {
            List<NutritionFood> defaults = nutritionFoodRepository.findAllByIsActiveTrueOrderByNameAsc();
            for (NutritionFood f : defaults) {
                if (!f.getVariants().isEmpty()) {
                    NutritionFoodVariant v = f.getVariants().get(0);
                    String key = "VAR_" + v.getId();
                    map.put(key, NutritionRecentFoodDto.builder()
                            .foodVariantId(v.getId())
                            .name(f.getName())
                            .state(v.getState())
                            .defaultQuantity(v.getServingAmount())
                            .defaultUnit(v.getServingUnit())
                            .isUserCustom(false)
                            .build());
                    if (map.size() >= 6) break;
                }
            }
        }

        return new ArrayList<>(map.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NutritionUserFoodDto> getUserFoods(Long userId) {
        List<NutritionUserFood> list = nutritionUserFoodRepository.findByUserIdOrderByNameAsc(userId);
        return list.stream().map(this::mapToUserFoodDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NutritionUserFoodDto createUserFood(Long userId, NutritionUserFoodRequest request) {
        NutritionUserFood userFood = NutritionUserFood.builder()
                .userId(userId)
                .name(request.getName().trim())
                .state(request.getState() != null ? request.getState().trim() : "Tươi/Chín")
                .servingAmount(request.getServingAmount() != null ? request.getServingAmount() : new BigDecimal("100"))
                .servingUnit(request.getServingUnit() != null ? request.getServingUnit().trim() : "g")
                .calories(request.getCalories())
                .protein(request.getProtein())
                .carbohydrate(request.getCarbohydrate())
                .fat(request.getFat())
                .fiber(request.getFiber() != null ? request.getFiber() : BigDecimal.ZERO)
                .dataSource(request.getDataSource() != null ? request.getDataSource().trim() : "Món cá nhân tự tạo")
                .description(request.getDescription())
                .build();

        NutritionUserFood saved = nutritionUserFoodRepository.save(userFood);

        if (request.getMicronutrients() != null && !request.getMicronutrients().isEmpty()) {
            for (NutritionMicronutrientDto mDto : request.getMicronutrients()) {
                if (mDto.getNutrientName() != null && !mDto.getNutrientName().trim().isEmpty() && mDto.getAmount() != null) {
                    NutritionUserFoodMicronutrient mEntity = NutritionUserFoodMicronutrient.builder()
                            .userFood(saved)
                            .nutrientName(mDto.getNutrientName().trim())
                            .amount(mDto.getAmount())
                            .unit(mDto.getUnit() != null ? mDto.getUnit().trim() : "mg")
                            .build();
                    nutritionUserFoodMicronutrientRepository.save(mEntity);
                }
            }
        }

        return mapToUserFoodDto(saved);
    }

    @Override
    @Transactional
    public void deleteUserFood(Long id, Long userId) {
        NutritionUserFood userFood = nutritionUserFoodRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thực phẩm cá nhân với ID: " + id));
        nutritionUserFoodRepository.delete(userFood);
    }

    // ================= HELPER MAPPERS =================

    private NutritionFoodSearchDto mapToFoodSearchDto(NutritionFood food) {
        List<NutritionVariantDto> variants = food.getVariants().stream().map(v -> {
            List<NutritionMicronutrientDto> micros = v.getMicronutrients().stream().map(m ->
                    NutritionMicronutrientDto.builder()
                            .id(m.getId())
                            .nutrientName(m.getNutrientName())
                            .amount(m.getAmount())
                            .unit(m.getUnit())
                            .build()
            ).collect(Collectors.toList());

            return NutritionVariantDto.builder()
                    .id(v.getId())
                    .state(v.getState())
                    .servingAmount(v.getServingAmount())
                    .servingUnit(v.getServingUnit())
                    .calories(v.getCalories())
                    .protein(v.getProtein())
                    .carbohydrate(v.getCarbohydrate())
                    .fat(v.getFat())
                    .fiber(v.getFiber())
                    .dataSource(v.getDataSource())
                    .micronutrients(micros)
                    .build();
        }).collect(Collectors.toList());

        List<NutritionUnitDto> units = food.getUnits().stream().map(u ->
                NutritionUnitDto.builder()
                        .id(u.getId())
                        .unit(u.getUnit())
                        .gramValue(u.getGramValue())
                        .mlValue(u.getMlValue())
                        .description(u.getDescription())
                        .build()
        ).collect(Collectors.toList());

        return NutritionFoodSearchDto.builder()
                .foodId(food.getId())
                .name(food.getName())
                .category(food.getCategory())
                .description(food.getDescription())
                .dataSource(food.getDataSource())
                .isUserCustom(false)
                .variants(variants)
                .units(units)
                .build();
    }

    private NutritionFoodSearchDto mapUserFoodToSearchDto(NutritionUserFood uFood) {
        List<NutritionMicronutrientDto> micros = uFood.getMicronutrients().stream().map(m ->
                NutritionMicronutrientDto.builder()
                        .id(m.getId())
                        .nutrientName(m.getNutrientName())
                        .amount(m.getAmount())
                        .unit(m.getUnit())
                        .build()
        ).collect(Collectors.toList());

        NutritionVariantDto variantDto = NutritionVariantDto.builder()
                .id(uFood.getId())
                .state(uFood.getState() != null ? uFood.getState() : "Tươi/Chín")
                .servingAmount(uFood.getServingAmount())
                .servingUnit(uFood.getServingUnit())
                .calories(uFood.getCalories())
                .protein(uFood.getProtein())
                .carbohydrate(uFood.getCarbohydrate())
                .fat(uFood.getFat())
                .fiber(uFood.getFiber())
                .dataSource(uFood.getDataSource())
                .micronutrients(micros)
                .build();

        return NutritionFoodSearchDto.builder()
                .userFoodId(uFood.getId())
                .name(uFood.getName())
                .category("Món cá nhân")
                .description(uFood.getDescription())
                .dataSource(uFood.getDataSource())
                .isUserCustom(true)
                .variants(Collections.singletonList(variantDto))
                .units(Collections.emptyList())
                .build();
    }

    private NutritionDailyLogResponse mapToDailyLogResponse(NutritionDailyLog log) {
        String foodName = "Không xác định";
        String state = "Mặc định";
        String dataSource = "Chưa rõ";
        boolean isUserCustom = false;
        List<NutritionMicronutrientDto> scaledMicros = new ArrayList<>();

        if (log.getFoodVariant() != null) {
            NutritionFoodVariant v = log.getFoodVariant();
            if (v.getFood() != null) {
                foodName = v.getFood().getName();
            }
            state = v.getState();
            dataSource = v.getDataSource() != null ? v.getDataSource() : (v.getFood() != null ? v.getFood().getDataSource() : "Hệ thống");

            // Calculate scale multiplier
            BigDecimal serving = v.getServingAmount() != null && v.getServingAmount().compareTo(BigDecimal.ZERO) > 0 ? v.getServingAmount() : new BigDecimal("100");
            BigDecimal baseAmount = log.getCalculatedGrams() != null ? log.getCalculatedGrams() : (log.getCalculatedMl() != null ? log.getCalculatedMl() : log.getQuantity());
            BigDecimal multiplier = baseAmount.divide(serving, 4, RoundingMode.HALF_UP);

            for (NutritionMicronutrient m : v.getMicronutrients()) {
                BigDecimal scaled = m.getAmount().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
                scaledMicros.add(NutritionMicronutrientDto.builder()
                        .id(m.getId())
                        .nutrientName(m.getNutrientName())
                        .amount(scaled)
                        .unit(m.getUnit())
                        .build());
            }
        } else if (log.getUserFood() != null) {
            NutritionUserFood u = log.getUserFood();
            foodName = u.getName();
            state = u.getState() != null ? u.getState() : "Tươi/Chín";
            dataSource = u.getDataSource() != null ? u.getDataSource() : "Món tự tạo";
            isUserCustom = true;

            BigDecimal serving = u.getServingAmount() != null && u.getServingAmount().compareTo(BigDecimal.ZERO) > 0 ? u.getServingAmount() : new BigDecimal("100");
            BigDecimal baseAmount = log.getCalculatedGrams() != null ? log.getCalculatedGrams() : (log.getCalculatedMl() != null ? log.getCalculatedMl() : log.getQuantity());
            BigDecimal multiplier = baseAmount.divide(serving, 4, RoundingMode.HALF_UP);

            for (NutritionUserFoodMicronutrient m : u.getMicronutrients()) {
                BigDecimal scaled = m.getAmount().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
                scaledMicros.add(NutritionMicronutrientDto.builder()
                        .id(m.getId())
                        .nutrientName(m.getNutrientName())
                        .amount(scaled)
                        .unit(m.getUnit())
                        .build());
            }
        }

        return NutritionDailyLogResponse.builder()
                .id(log.getId())
                .logDate(log.getLogDate())
                .foodVariantId(log.getFoodVariant() != null ? log.getFoodVariant().getId() : null)
                .userFoodId(log.getUserFood() != null ? log.getUserFood().getId() : null)
                .foodName(foodName)
                .state(state)
                .quantity(log.getQuantity())
                .unit(log.getUnit())
                .calculatedGrams(log.getCalculatedGrams())
                .calculatedMl(log.getCalculatedMl())
                .calories(log.getCalories() != null ? log.getCalories().setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .protein(log.getProtein() != null ? log.getProtein().setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .carbohydrate(log.getCarbohydrate() != null ? log.getCarbohydrate().setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .fat(log.getFat() != null ? log.getFat().setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .fiber(log.getFiber() != null ? log.getFiber().setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .isUserCustom(isUserCustom)
                .dataSource(dataSource)
                .micronutrients(scaledMicros)
                .build();
    }

    private NutritionUserFoodDto mapToUserFoodDto(NutritionUserFood u) {
        List<NutritionMicronutrientDto> micros = u.getMicronutrients().stream().map(m ->
                NutritionMicronutrientDto.builder()
                        .id(m.getId())
                        .nutrientName(m.getNutrientName())
                        .amount(m.getAmount())
                        .unit(m.getUnit())
                        .build()
        ).collect(Collectors.toList());

        return NutritionUserFoodDto.builder()
                .id(u.getId())
                .name(u.getName())
                .state(u.getState())
                .servingAmount(u.getServingAmount())
                .servingUnit(u.getServingUnit())
                .calories(u.getCalories())
                .protein(u.getProtein())
                .carbohydrate(u.getCarbohydrate())
                .fat(u.getFat())
                .fiber(u.getFiber())
                .dataSource(u.getDataSource())
                .description(u.getDescription())
                .micronutrients(micros)
                .build();
    }

    // ================= INITIAL TEST / VERIFIED SEED DATA =================
    @Override
    @Transactional
    public void initSampleFoodsIfEmpty() {
        if (nutritionFoodRepository.count() > 0) {
            return;
        }
        log.info("Initializing verified test nutrition data (USDA & Vietnam Food Composition Table)...");

        // 1. Ức gà (Chicken Breast) - Raw & Cooked
        NutritionFood chicken = NutritionFood.builder()
                .name("Ức gà")
                .category("Thịt / Gia cầm")
                .description("Ức gà không da, giàu protein chất lượng cao")
                .dataSource("USDA FoodData Central")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(chicken);

        NutritionFoodVariant chickenRaw = NutritionFoodVariant.builder()
                .food(chicken)
                .state("Sống")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("120"))
                .protein(new BigDecimal("23.0"))
                .carbohydrate(new BigDecimal("0.0"))
                .fat(new BigDecimal("2.5"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC ID: 171077 (Raw)")
                .build();
        chickenRaw.getMicronutrients().add(NutritionMicronutrient.builder().variant(chickenRaw).nutrientName("Vitamin B6").amount(new BigDecimal("0.6")).unit("mg").build());
        chickenRaw.getMicronutrients().add(NutritionMicronutrient.builder().variant(chickenRaw).nutrientName("Kali").amount(new BigDecimal("256")).unit("mg").build());
        chickenRaw.getMicronutrients().add(NutritionMicronutrient.builder().variant(chickenRaw).nutrientName("Selenium").amount(new BigDecimal("27.6")).unit("mcg").build());
        nutritionFoodVariantRepository.save(chickenRaw);

        NutritionFoodVariant chickenCooked = NutritionFoodVariant.builder()
                .food(chicken)
                .state("Chín")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("165"))
                .protein(new BigDecimal("31.0"))
                .carbohydrate(new BigDecimal("0.0"))
                .fat(new BigDecimal("3.6"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC ID: 171477 (Cooked)")
                .build();
        chickenCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(chickenCooked).nutrientName("Vitamin B6").amount(new BigDecimal("0.8")).unit("mg").build());
        chickenCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(chickenCooked).nutrientName("Kali").amount(new BigDecimal("334")).unit("mg").build());
        chickenCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(chickenCooked).nutrientName("Selenium").amount(new BigDecimal("36.4")).unit("mcg").build());
        nutritionFoodVariantRepository.save(chickenCooked);

        NutritionFoodVariant chickenBoiled = NutritionFoodVariant.builder()
                .food(chicken)
                .state("Luộc")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("165"))
                .protein(new BigDecimal("31.0"))
                .carbohydrate(new BigDecimal("0.0"))
                .fat(new BigDecimal("3.6"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC ID: 171477 (Boiled)")
                .build();
        nutritionFoodVariantRepository.save(chickenBoiled);

        NutritionFoodVariant chickenGrilled = NutritionFoodVariant.builder()
                .food(chicken)
                .state("Nướng")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("170"))
                .protein(new BigDecimal("31.5"))
                .carbohydrate(new BigDecimal("0.0"))
                .fat(new BigDecimal("4.0"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC (Grilled)")
                .build();
        nutritionFoodVariantRepository.save(chickenGrilled);

        NutritionFoodVariant chickenFried = NutritionFoodVariant.builder()
                .food(chicken)
                .state("Chiên")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("220"))
                .protein(new BigDecimal("28.0"))
                .carbohydrate(new BigDecimal("2.0"))
                .fat(new BigDecimal("11.0"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC (Fried)")
                .build();
        nutritionFoodVariantRepository.save(chickenFried);

        // Chicken units
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(chicken).unit("miếng").gramValue(new BigDecimal("150")).description("1 miếng ức gà trung bình ~ 150g").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(chicken).unit("khẩu phần").gramValue(new BigDecimal("200")).description("1 khẩu phần ăn ~ 200g").build());

        // 2. Cơm trắng (Cooked White Rice)
        NutritionFood rice = NutritionFood.builder()
                .name("Cơm trắng")
                .category("Tinh bột / Ngũ cốc")
                .description("Cơm gạo tẻ nấu chín")
                .dataSource("Bảng thành phần thực phẩm VN / USDA")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(rice);

        NutritionFoodVariant riceCooked = NutritionFoodVariant.builder()
                .food(rice)
                .state("Chín")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("130"))
                .protein(new BigDecimal("2.7"))
                .carbohydrate(new BigDecimal("28.2"))
                .fat(new BigDecimal("0.3"))
                .fiber(new BigDecimal("0.4"))
                .dataSource("Bảng TP TP VN (Viện Dinh Dưỡng)")
                .build();
        riceCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(riceCooked).nutrientName("Magie").amount(new BigDecimal("12")).unit("mg").build());
        riceCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(riceCooked).nutrientName("Kali").amount(new BigDecimal("35")).unit("mg").build());
        nutritionFoodVariantRepository.save(riceCooked);

        // 1 bát cơm ăn cơm chuẩn VN ~ 150g cơm chín (tương đương ~195 kcal, 4.0g Protein, 42.3g Carb)
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(rice).unit("bát").gramValue(new BigDecimal("150")).description("1 bát cơm đầy vừa ~ 150g cơm chín").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(rice).unit("chén").gramValue(new BigDecimal("150")).description("1 chén cơm ~ 150g").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(rice).unit("thìa").gramValue(new BigDecimal("25")).description("1 thìa xới cơm ~ 25g").build());

        // 3. Trứng gà (Eggs)
        NutritionFood egg = NutritionFood.builder()
                .name("Trứng gà")
                .category("Trứng")
                .description("Trứng gà tươi hoặc luộc chín")
                .dataSource("USDA FoodData Central")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(egg);

        NutritionFoodVariant eggCooked = NutritionFoodVariant.builder()
                .food(egg)
                .state("Chín")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("155"))
                .protein(new BigDecimal("13.0"))
                .carbohydrate(new BigDecimal("1.1"))
                .fat(new BigDecimal("11.0"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC (Boiled egg)")
                .build();
        eggCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(eggCooked).nutrientName("Vitamin B12").amount(new BigDecimal("1.1")).unit("mcg").build());
        eggCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(eggCooked).nutrientName("Vitamin A").amount(new BigDecimal("160")).unit("mcg").build());
        eggCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(eggCooked).nutrientName("Canxi").amount(new BigDecimal("50")).unit("mg").build());
        eggCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(eggCooked).nutrientName("Selenium").amount(new BigDecimal("30.8")).unit("mcg").build());
        nutritionFoodVariantRepository.save(eggCooked);

        NutritionFoodVariant eggRaw = NutritionFoodVariant.builder()
                .food(egg)
                .state("Sống")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("143"))
                .protein(new BigDecimal("12.6"))
                .carbohydrate(new BigDecimal("0.7"))
                .fat(new BigDecimal("9.5"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC (Raw egg)")
                .build();
        nutritionFoodVariantRepository.save(eggRaw);

        NutritionFoodVariant eggFried = NutritionFoodVariant.builder()
                .food(egg)
                .state("Chiên")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("196"))
                .protein(new BigDecimal("13.6"))
                .carbohydrate(new BigDecimal("0.8"))
                .fat(new BigDecimal("15.3"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC (Fried egg with oil)")
                .build();
        nutritionFoodVariantRepository.save(eggFried);

        // 1 quả trứng gà trung bình ~ 50g phần ăn được
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(egg).unit("quả").gramValue(new BigDecimal("50")).description("1 quả trứng gà ~ 50g").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(egg).unit("cái").gramValue(new BigDecimal("50")).description("1 cái trứng ~ 50g").build());

        // 4. Bưởi (Pomelo / Grapefruit)
        NutritionFood pomelo = NutritionFood.builder()
                .name("Bưởi")
                .category("Trái cây")
                .description("Múi bưởi tươi mọng nước, giàu Vitamin C")
                .dataSource("USDA / Bảng TP TP VN")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(pomelo);

        NutritionFoodVariant pomeloFresh = NutritionFoodVariant.builder()
                .food(pomelo)
                .state("Tươi")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("38"))
                .protein(new BigDecimal("0.8"))
                .carbohydrate(new BigDecimal("9.6"))
                .fat(new BigDecimal("0.04"))
                .fiber(new BigDecimal("1.0"))
                .dataSource("USDA FDC ID: 170068")
                .build();
        pomeloFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(pomeloFresh).nutrientName("Vitamin C").amount(new BigDecimal("61")).unit("mg").build());
        pomeloFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(pomeloFresh).nutrientName("Kali").amount(new BigDecimal("216")).unit("mg").build());
        nutritionFoodVariantRepository.save(pomeloFresh);

        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(pomelo).unit("quả").gramValue(new BigDecimal("600")).description("1 quả bưởi (phần múi ăn được) ~ 600g").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(pomelo).unit("múi").gramValue(new BigDecimal("40")).description("1 múi bưởi ~ 40g").build());

        // 5. Đậu phụ (Tofu)
        NutritionFood tofu = NutritionFood.builder()
                .name("Đậu phụ")
                .category("Thực vật giàu Protein")
                .description("Đậu phụ trắng mềm truyền thống")
                .dataSource("Bảng TP TP VN")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(tofu);

        NutritionFoodVariant tofuFresh = NutritionFoodVariant.builder()
                .food(tofu)
                .state("Tươi")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("76"))
                .protein(new BigDecimal("8.1"))
                .carbohydrate(new BigDecimal("1.9"))
                .fat(new BigDecimal("4.8"))
                .fiber(new BigDecimal("0.3"))
                .dataSource("Bảng TP TP VN (Đậu phụ trắng)")
                .build();
        tofuFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(tofuFresh).nutrientName("Canxi").amount(new BigDecimal("350")).unit("mg").build());
        tofuFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(tofuFresh).nutrientName("Magie").amount(new BigDecimal("30")).unit("mg").build());
        nutritionFoodVariantRepository.save(tofuFresh);

        NutritionFoodVariant tofuFried = NutritionFoodVariant.builder()
                .food(tofu)
                .state("Chiên")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("190"))
                .protein(new BigDecimal("15.0"))
                .carbohydrate(new BigDecimal("3.0"))
                .fat(new BigDecimal("13.5"))
                .fiber(new BigDecimal("0.5"))
                .dataSource("Bảng TP TP VN (Đậu phụ rán)")
                .build();
        nutritionFoodVariantRepository.save(tofuFried);

        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(tofu).unit("miếng").gramValue(new BigDecimal("150")).description("1 bìa/miếng đậu phụ ~ 150g").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(tofu).unit("bìa").gramValue(new BigDecimal("150")).description("1 bìa đậu phụ ~ 150g").build());

        // 6. Mướp (Luffa / Sponge Gourd)
        NutritionFood luffa = NutritionFood.builder()
                .name("Mướp")
                .category("Rau củ")
                .description("Mướp hương tươi, nấu canh hoặc luộc")
                .dataSource("Bảng TP TP VN")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(luffa);

        NutritionFoodVariant luffaFresh = NutritionFoodVariant.builder()
                .food(luffa)
                .state("Tươi")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("16"))
                .protein(new BigDecimal("0.9"))
                .carbohydrate(new BigDecimal("3.0"))
                .fat(new BigDecimal("0.1"))
                .fiber(new BigDecimal("0.5"))
                .dataSource("Bảng TP TP VN")
                .build();
        luffaFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(luffaFresh).nutrientName("Vitamin C").amount(new BigDecimal("12")).unit("mg").build());
        luffaFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(luffaFresh).nutrientName("Kali").amount(new BigDecimal("140")).unit("mg").build());
        nutritionFoodVariantRepository.save(luffaFresh);

        NutritionFoodVariant luffaBoiled = NutritionFoodVariant.builder()
                .food(luffa)
                .state("Luộc")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("14"))
                .protein(new BigDecimal("0.8"))
                .carbohydrate(new BigDecimal("2.6"))
                .fat(new BigDecimal("0.1"))
                .fiber(new BigDecimal("0.5"))
                .dataSource("Bảng TP TP VN (Mướp luộc)")
                .build();
        nutritionFoodVariantRepository.save(luffaBoiled);

        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(luffa).unit("quả").gramValue(new BigDecimal("200")).description("1 quả mướp vừa ~ 200g phần ăn được").build());

        // 7. Chuối (Banana)
        NutritionFood banana = NutritionFood.builder()
                .name("Chuối")
                .category("Trái cây")
                .description("Chuối tiêu chín tự nhiên")
                .dataSource("USDA FoodData Central")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(banana);

        NutritionFoodVariant bananaFresh = NutritionFoodVariant.builder()
                .food(banana)
                .state("Tươi")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("89"))
                .protein(new BigDecimal("1.1"))
                .carbohydrate(new BigDecimal("22.8"))
                .fat(new BigDecimal("0.3"))
                .fiber(new BigDecimal("2.6"))
                .dataSource("USDA FDC ID: 173944")
                .build();
        bananaFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(bananaFresh).nutrientName("Kali").amount(new BigDecimal("358")).unit("mg").build());
        bananaFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(bananaFresh).nutrientName("Vitamin B6").amount(new BigDecimal("0.4")).unit("mg").build());
        bananaFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(bananaFresh).nutrientName("Vitamin C").amount(new BigDecimal("8.7")).unit("mg").build());
        bananaFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(bananaFresh).nutrientName("Magie").amount(new BigDecimal("27")).unit("mg").build());
        nutritionFoodVariantRepository.save(bananaFresh);

        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(banana).unit("quả").gramValue(new BigDecimal("118")).description("1 quả chuối tiêu vừa (bỏ vỏ) ~ 118g").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(banana).unit("trái").gramValue(new BigDecimal("118")).description("1 trái chuối ~ 118g").build());

        // 8. Dầu ăn (Cooking Oil)
        NutritionFood oil = NutritionFood.builder()
                .name("Dầu ăn")
                .category("Dầu mỡ / Gia vị")
                .description("Dầu thực vật nguyên chất (100% lipid)")
                .dataSource("USDA FoodData Central")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(oil);

        NutritionFoodVariant oilVariant = NutritionFoodVariant.builder()
                .food(oil)
                .state("Nước")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("ml")
                .calories(new BigDecimal("884"))
                .protein(new BigDecimal("0.0"))
                .carbohydrate(new BigDecimal("0.0"))
                .fat(new BigDecimal("100.0"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC (Vegetable Oil)")
                .build();
        oilVariant.getMicronutrients().add(NutritionMicronutrient.builder().variant(oilVariant).nutrientName("Vitamin E").amount(new BigDecimal("14.3")).unit("mg").build());
        nutritionFoodVariantRepository.save(oilVariant);

        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(oil).unit("thìa").mlValue(new BigDecimal("10")).description("1 thìa canh dầu ăn ~ 10ml (~88 kcal, 10g fat)").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(oil).unit("muỗng").mlValue(new BigDecimal("10")).description("1 muỗng dầu ~ 10ml").build());

        // 9. Thịt bò nạc (Lean Beef)
        NutritionFood beef = NutritionFood.builder()
                .name("Thịt bò")
                .category("Thịt / Gia súc")
                .description("Thịt bò nạc (thăn bò)")
                .dataSource("USDA FoodData Central")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(beef);

        NutritionFoodVariant beefRaw = NutritionFoodVariant.builder()
                .food(beef)
                .state("Sống")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("143"))
                .protein(new BigDecimal("21.5"))
                .carbohydrate(new BigDecimal("0.0"))
                .fat(new BigDecimal("5.8"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC ID: 170208 (Raw Beef)")
                .build();
        beefRaw.getMicronutrients().add(NutritionMicronutrient.builder().variant(beefRaw).nutrientName("Sắt").amount(new BigDecimal("2.6")).unit("mg").build());
        beefRaw.getMicronutrients().add(NutritionMicronutrient.builder().variant(beefRaw).nutrientName("Kẽm").amount(new BigDecimal("4.8")).unit("mg").build());
        beefRaw.getMicronutrients().add(NutritionMicronutrient.builder().variant(beefRaw).nutrientName("Vitamin B12").amount(new BigDecimal("2.2")).unit("mcg").build());
        nutritionFoodVariantRepository.save(beefRaw);

        NutritionFoodVariant beefCooked = NutritionFoodVariant.builder()
                .food(beef)
                .state("Chín")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("217"))
                .protein(new BigDecimal("26.1"))
                .carbohydrate(new BigDecimal("0.0"))
                .fat(new BigDecimal("11.8"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("USDA FDC (Cooked Beef)")
                .build();
        beefCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(beefCooked).nutrientName("Sắt").amount(new BigDecimal("3.1")).unit("mg").build());
        beefCooked.getMicronutrients().add(NutritionMicronutrient.builder().variant(beefCooked).nutrientName("Kẽm").amount(new BigDecimal("6.0")).unit("mg").build());
        nutritionFoodVariantRepository.save(beefCooked);

        // 10. Rau cải (Mustard Greens)
        NutritionFood mustardGreens = NutritionFood.builder()
                .name("Rau cải")
                .category("Rau củ")
                .description("Rau cải xanh tươi hoặc luộc")
                .dataSource("Bảng TP TP VN")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(mustardGreens);

        NutritionFoodVariant mustardFresh = NutritionFoodVariant.builder()
                .food(mustardGreens)
                .state("Tươi")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("17"))
                .protein(new BigDecimal("1.7"))
                .carbohydrate(new BigDecimal("2.1"))
                .fat(new BigDecimal("0.2"))
                .fiber(new BigDecimal("1.8"))
                .dataSource("Bảng TP TP VN")
                .build();
        mustardFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(mustardFresh).nutrientName("Vitamin C").amount(new BigDecimal("51")).unit("mg").build());
        mustardFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(mustardFresh).nutrientName("Canxi").amount(new BigDecimal("89")).unit("mg").build());
        mustardFresh.getMicronutrients().add(NutritionMicronutrient.builder().variant(mustardFresh).nutrientName("Vitamin A").amount(new BigDecimal("302")).unit("mcg").build());
        nutritionFoodVariantRepository.save(mustardFresh);

        NutritionFoodVariant mustardBoiled = NutritionFoodVariant.builder()
                .food(mustardGreens)
                .state("Luộc")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("15"))
                .protein(new BigDecimal("1.5"))
                .carbohydrate(new BigDecimal("1.8"))
                .fat(new BigDecimal("0.2"))
                .fiber(new BigDecimal("1.6"))
                .dataSource("Bảng TP TP VN (Luộc)")
                .build();
        nutritionFoodVariantRepository.save(mustardBoiled);

        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(mustardGreens).unit("bát").gramValue(new BigDecimal("100")).description("1 bát rau luộc ~ 100g").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(mustardGreens).unit("đĩa").gramValue(new BigDecimal("250")).description("1 đĩa rau luộc ~ 250g").build());

        // 11. Bún tươi (Fresh Rice Noodles)
        NutritionFood bun = NutritionFood.builder()
                .name("Bún tươi")
                .category("Tinh bột / Ngũ cốc")
                .description("Bún gạo tươi")
                .dataSource("Bảng TP TP VN")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(bun);

        NutritionFoodVariant bunFresh = NutritionFoodVariant.builder()
                .food(bun)
                .state("Tươi")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("g")
                .calories(new BigDecimal("110"))
                .protein(new BigDecimal("1.7"))
                .carbohydrate(new BigDecimal("25.7"))
                .fat(new BigDecimal("0.0"))
                .fiber(new BigDecimal("0.3"))
                .dataSource("Bảng TP TP VN")
                .build();
        nutritionFoodVariantRepository.save(bunFresh);

        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(bun).unit("bát").gramValue(new BigDecimal("180")).description("1 bát bún ~ 180g").build());

        // 12. Sữa tươi không đường (Unsweetened Fresh Milk)
        NutritionFood milk = NutritionFood.builder()
                .name("Sữa tươi không đường")
                .category("Sữa / Đồ uống")
                .description("Sữa tươi tiệt trùng không đường")
                .dataSource("Nhà sản xuất Vinamilk / TH True Milk")
                .isActive(true)
                .build();
        nutritionFoodRepository.save(milk);

        NutritionFoodVariant milkVariant = NutritionFoodVariant.builder()
                .food(milk)
                .state("Nước")
                .servingAmount(new BigDecimal("100"))
                .servingUnit("ml")
                .calories(new BigDecimal("60"))
                .protein(new BigDecimal("3.0"))
                .carbohydrate(new BigDecimal("4.2"))
                .fat(new BigDecimal("3.4"))
                .fiber(new BigDecimal("0.0"))
                .dataSource("Vinamilk 100% Sữa tươi")
                .build();
        milkVariant.getMicronutrients().add(NutritionMicronutrient.builder().variant(milkVariant).nutrientName("Canxi").amount(new BigDecimal("110")).unit("mg").build());
        milkVariant.getMicronutrients().add(NutritionMicronutrient.builder().variant(milkVariant).nutrientName("Vitamin D3").amount(new BigDecimal("1.2")).unit("mcg").build());
        nutritionFoodVariantRepository.save(milkVariant);

        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(milk).unit("hộp").mlValue(new BigDecimal("180")).description("1 hộp sữa ~ 180ml (~108 kcal, 5.4g Protein)").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(milk).unit("cốc").mlValue(new BigDecimal("220")).description("1 cốc sữa ~ 220ml").build());
        nutritionFoodUnitRepository.save(NutritionFoodUnit.builder().food(milk).unit("ly").mlValue(new BigDecimal("220")).description("1 ly sữa ~ 220ml").build());

        log.info("Finished initializing verified sample nutrition data successfully!");
    }
}
