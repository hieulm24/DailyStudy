package com.englishlearning.service;

import com.englishlearning.dto.nutrition.NutritionFoodSearchDto;
import com.englishlearning.dto.nutrition.NutritionMicronutrientDto;
import com.englishlearning.dto.nutrition.UsdaFoodDto;
import com.englishlearning.entity.nutrition.NutritionFood;
import com.englishlearning.entity.nutrition.NutritionFoodUnit;
import com.englishlearning.entity.nutrition.NutritionFoodVariant;
import com.englishlearning.entity.nutrition.NutritionMicronutrient;
import com.englishlearning.repository.nutrition.NutritionFoodRepository;
import com.englishlearning.repository.nutrition.NutritionFoodUnitRepository;
import com.englishlearning.repository.nutrition.NutritionFoodVariantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsdaFoodDataService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final NutritionFoodRepository nutritionFoodRepository;
    private final NutritionFoodVariantRepository nutritionFoodVariantRepository;
    private final NutritionFoodUnitRepository nutritionFoodUnitRepository;

    @Value("${app.nutrition.usda.api-key:DEMO_KEY}")
    private String apiKey;

    @Value("${app.nutrition.usda.base-url:https://api.nal.usda.gov/fdc/v1}")
    private String baseUrl;

    private final Map<Long, UsdaFoodDto> searchCache = new java.util.concurrent.ConcurrentHashMap<>();

    public List<UsdaFoodDto> searchFoods(String query, Integer pageSize) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        int size = (pageSize != null && pageSize > 0 && pageSize <= 25) ? pageSize : 10;
        List<UsdaFoodDto> results = new ArrayList<>();

        try {
            String uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/foods/search")
                    .queryParam("query", query.trim())
                    .queryParam("pageSize", size)
                    .queryParam("api_key", apiKey)
                    .build()
                    .toUriString();

            log.info("Querying USDA FoodData Central API for: {}", query);
            String jsonResponse = restTemplate.getForObject(uri, String.class);
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return results;
            }

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode foodsNode = root.path("foods");

            if (foodsNode.isArray()) {
                for (JsonNode foodNode : foodsNode) {
                    UsdaFoodDto dto = parseFoodNode(foodNode);
                    if (dto != null && dto.getFdcId() != null) {
                        searchCache.put(dto.getFdcId(), dto);
                        results.add(dto);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error querying USDA FoodData Central API: {}", e.getMessage(), e);
        }

        return results;
    }

    @Transactional
    public NutritionFood importUsdaFoodToSystem(Long fdcId) {
        if (fdcId == null) {
            throw new IllegalArgumentException("FDC ID không được để trống");
        }

        try {
            UsdaFoodDto dto = searchCache.get(fdcId);
            if (dto == null) {
                String uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/food/" + fdcId)
                        .queryParam("api_key", apiKey)
                        .build()
                        .toUriString();

                log.info("Fetching details for FDC ID: {}", fdcId);
                String jsonResponse = restTemplate.getForObject(uri, String.class);
                JsonNode foodNode = objectMapper.readTree(jsonResponse);
                dto = parseFoodNode(foodNode);
            }

            if (dto == null) {
                throw new IllegalStateException("Không thể đọc thông tin dinh dưỡng từ USDA");
            }

            // Check if food with this name already exists in database
            String foodName = dto.getDescription();
            if (foodName != null && foodName.length() > 200) {
                foodName = foodName.substring(0, 200);
            }

            java.util.Optional<NutritionFood> existingFood = nutritionFoodRepository.findFirstByNameIgnoreCase(foodName);
            if (existingFood.isPresent()) {
                return existingFood.get();
            }

            // Save to NUTRITION_FOOD
            NutritionFood food = NutritionFood.builder()
                    .name(foodName)
                    .category(dto.getDataType() != null ? dto.getDataType() : "USDA Food")
                    .description("Nhập từ USDA FoodData Central (FDC ID: " + fdcId + ")")
                    .dataSource("USDA FoodData Central (FDC ID: " + fdcId + ")")
                    .isActive(true)
                    .build();
            NutritionFood savedFood = nutritionFoodRepository.save(food);

            // Save variant
            BigDecimal servingAmount = dto.getServingSize() != null && dto.getServingSize().compareTo(BigDecimal.ZERO) > 0
                    ? dto.getServingSize() : new BigDecimal("100");
            String servingUnit = dto.getServingSizeUnit() != null ? dto.getServingSizeUnit() : "g";

            NutritionFoodVariant variant = NutritionFoodVariant.builder()
                    .food(savedFood)
                    .state("Tiêu chuẩn")
                    .servingAmount(servingAmount)
                    .servingUnit(servingUnit)
                    .calories(dto.getCalories() != null ? dto.getCalories() : BigDecimal.ZERO)
                    .protein(dto.getProtein() != null ? dto.getProtein() : BigDecimal.ZERO)
                    .carbohydrate(dto.getCarbohydrate() != null ? dto.getCarbohydrate() : BigDecimal.ZERO)
                    .fat(dto.getFat() != null ? dto.getFat() : BigDecimal.ZERO)
                    .fiber(dto.getFiber() != null ? dto.getFiber() : BigDecimal.ZERO)
                    .dataSource("USDA FDC: " + fdcId)
                    .build();

            for (NutritionMicronutrientDto micro : dto.getMicronutrients()) {
                variant.getMicronutrients().add(NutritionMicronutrient.builder()
                        .variant(variant)
                        .nutrientName(micro.getNutrientName())
                        .amount(micro.getAmount())
                        .unit(micro.getUnit())
                        .build());
            }

            nutritionFoodVariantRepository.save(variant);

            // Add standard serving unit
            nutritionFoodUnitRepository.save(NutritionFoodUnit.builder()
                    .food(savedFood)
                    .unit("khẩu phần")
                    .gramValue(servingAmount)
                    .description("1 khẩu phần ăn ~ " + servingAmount + servingUnit)
                    .build());

            return savedFood;
        } catch (Exception e) {
            log.error("Failed to import food from USDA: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể nhập món từ USDA: " + e.getMessage());
        }
    }

    private UsdaFoodDto parseFoodNode(JsonNode foodNode) {
        if (foodNode == null || foodNode.isMissingNode()) return null;

        Long fdcId = foodNode.path("fdcId").asLong();
        String description = foodNode.path("description").asText("Thực phẩm USDA");
        String dataType = foodNode.path("dataType").asText("");
        String brandOwner = foodNode.path("brandOwner").asText("");

        BigDecimal servingSize = null;
        if (foodNode.has("servingSize") && !foodNode.get("servingSize").isNull()) {
            servingSize = new BigDecimal(foodNode.path("servingSize").asText()).setScale(2, RoundingMode.HALF_UP);
        }
        String servingSizeUnit = foodNode.path("servingSizeUnit").asText("g");

        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal carb = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
        BigDecimal fiber = BigDecimal.ZERO;

        List<NutritionMicronutrientDto> micronutrients = new ArrayList<>();

        JsonNode nutrientsNode = foodNode.path("foodNutrients");
        if (nutrientsNode.isArray()) {
            for (JsonNode n : nutrientsNode) {
                String nutrientName = n.path("nutrientName").asText("");
                String unitName = n.path("unitName").asText("").toUpperCase();
                BigDecimal value = BigDecimal.ZERO;

                if (n.has("value") && !n.get("value").isNull()) {
                    try {
                        value = new BigDecimal(n.path("value").asText()).setScale(2, RoundingMode.HALF_UP);
                    } catch (Exception ignored) {}
                } else if (n.has("amount") && !n.get("amount").isNull()) {
                    try {
                        value = new BigDecimal(n.path("amount").asText()).setScale(2, RoundingMode.HALF_UP);
                    } catch (Exception ignored) {}
                }

                String lower = nutrientName.toLowerCase();
                if (lower.contains("energy") && (unitName.contains("KCAL") || unitName.isEmpty())) {
                    if (calories.compareTo(BigDecimal.ZERO) == 0) {
                        calories = value;
                    }
                } else if (lower.equals("protein")) {
                    protein = value;
                } else if (lower.contains("carbohydrate")) {
                    carb = value;
                } else if (lower.contains("total lipid") || lower.equals("fat")) {
                    fat = value;
                } else if (lower.contains("fiber")) {
                    fiber = value;
                } else if (lower.contains("vitamin c")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Vitamin C").amount(value).unit(unitName.toLowerCase()).build());
                } else if (lower.contains("vitamin b-6")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Vitamin B6").amount(value).unit(unitName.toLowerCase()).build());
                } else if (lower.contains("vitamin b-12")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Vitamin B12").amount(value).unit(unitName.toLowerCase()).build());
                } else if (lower.contains("vitamin a")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Vitamin A").amount(value).unit(unitName.toLowerCase()).build());
                } else if (lower.contains("vitamin d")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Vitamin D").amount(value).unit(unitName.toLowerCase()).build());
                } else if (lower.contains("potassium")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Kali").amount(value).unit(unitName.toLowerCase()).build());
                } else if (lower.contains("calcium")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Canxi").amount(value).unit(unitName.toLowerCase()).build());
                } else if (lower.contains("magnesium")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Magie").amount(value).unit(unitName.toLowerCase()).build());
                } else if (lower.contains("iron")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Sắt").amount(value).unit(unitName.toLowerCase()).build());
                } else if (lower.contains("zinc")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Kẽm").amount(value).unit(unitName.toLowerCase()).build());
                } else if (lower.contains("selenium")) {
                    micronutrients.add(NutritionMicronutrientDto.builder().nutrientName("Selenium").amount(value).unit(unitName.toLowerCase()).build());
                }
            }
        }

        return UsdaFoodDto.builder()
                .fdcId(fdcId)
                .description(description)
                .dataType(dataType)
                .brandOwner(brandOwner)
                .servingSize(servingSize != null ? servingSize : new BigDecimal("100"))
                .servingSizeUnit(servingSizeUnit)
                .calories(calories)
                .protein(protein)
                .carbohydrate(carb)
                .fat(fat)
                .fiber(fiber)
                .micronutrients(micronutrients)
                .build();
    }
}
