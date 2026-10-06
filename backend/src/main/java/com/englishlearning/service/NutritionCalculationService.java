package com.englishlearning.service;

import com.englishlearning.entity.nutrition.*;
import com.englishlearning.repository.nutrition.NutritionFoodUnitRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NutritionCalculationService {

    private final NutritionFoodUnitRepository nutritionFoodUnitRepository;

    @Data
    @Builder
    public static class CalculatedMacroResult {
        private BigDecimal calculatedGrams;
        private BigDecimal calculatedMl;
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbohydrate;
        private BigDecimal fat;
        private BigDecimal fiber;
        private BigDecimal multiplier;
    }

    public CalculatedMacroResult calculateForVariant(NutritionFoodVariant variant, BigDecimal quantity, String unit) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        BigDecimal servingAmount = variant.getServingAmount() != null && variant.getServingAmount().compareTo(BigDecimal.ZERO) > 0
                ? variant.getServingAmount() : new BigDecimal("100");
        String servingUnit = variant.getServingUnit() != null ? variant.getServingUnit().trim().toLowerCase() : "g";

        BigDecimal calculatedGrams = null;
        BigDecimal calculatedMl = null;
        BigDecimal multiplier;

        String normalizedUnit = unit != null ? unit.trim().toLowerCase() : "g";

        if (normalizedUnit.equals("g") || normalizedUnit.equals("gram") || normalizedUnit.equals("grams")) {
            calculatedGrams = quantity;
            multiplier = quantity.divide(servingAmount, 6, RoundingMode.HALF_UP);
        } else if (normalizedUnit.equals("kg") || normalizedUnit.equals("kilogram")) {
            calculatedGrams = quantity.multiply(new BigDecimal("1000"));
            multiplier = calculatedGrams.divide(servingAmount, 6, RoundingMode.HALF_UP);
        } else if (normalizedUnit.equals("ml") || normalizedUnit.equals("milliliter")) {
            calculatedMl = quantity;
            multiplier = quantity.divide(servingAmount, 6, RoundingMode.HALF_UP);
        } else if (normalizedUnit.equals("l") || normalizedUnit.equals("liter") || normalizedUnit.equals("lít")) {
            calculatedMl = quantity.multiply(new BigDecimal("1000"));
            multiplier = calculatedMl.divide(servingAmount, 6, RoundingMode.HALF_UP);
        } else {
            // Check conversion in NUTRITION_FOOD_UNIT for this food
            Optional<NutritionFoodUnit> foodUnitOpt = Optional.empty();
            if (variant.getFood() != null && variant.getFood().getId() != null) {
                foodUnitOpt = nutritionFoodUnitRepository.findByFoodIdAndUnitIgnoreCase(variant.getFood().getId(), unit.trim());
            }

            if (foodUnitOpt.isPresent()) {
                NutritionFoodUnit foodUnit = foodUnitOpt.get();
                if (foodUnit.getGramValue() != null && foodUnit.getGramValue().compareTo(BigDecimal.ZERO) > 0) {
                    calculatedGrams = quantity.multiply(foodUnit.getGramValue());
                    multiplier = calculatedGrams.divide(servingAmount, 6, RoundingMode.HALF_UP);
                } else if (foodUnit.getMlValue() != null && foodUnit.getMlValue().compareTo(BigDecimal.ZERO) > 0) {
                    calculatedMl = quantity.multiply(foodUnit.getMlValue());
                    multiplier = calculatedMl.divide(servingAmount, 6, RoundingMode.HALF_UP);
                } else {
                    multiplier = quantity;
                }
            } else if (normalizedUnit.equalsIgnoreCase(servingUnit)) {
                multiplier = quantity.divide(servingAmount, 6, RoundingMode.HALF_UP);
            } else {
                // Fallback 1:1 if unknown unit
                multiplier = quantity;
            }
        }

        BigDecimal cal = variant.getCalories() != null ? variant.getCalories().multiply(multiplier).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal pro = variant.getProtein() != null ? variant.getProtein().multiply(multiplier).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal carb = variant.getCarbohydrate() != null ? variant.getCarbohydrate().multiply(multiplier).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal fat = variant.getFat() != null ? variant.getFat().multiply(multiplier).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal fib = variant.getFiber() != null ? variant.getFiber().multiply(multiplier).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return CalculatedMacroResult.builder()
                .calculatedGrams(calculatedGrams != null ? calculatedGrams.setScale(2, RoundingMode.HALF_UP) : null)
                .calculatedMl(calculatedMl != null ? calculatedMl.setScale(2, RoundingMode.HALF_UP) : null)
                .calories(cal)
                .protein(pro)
                .carbohydrate(carb)
                .fat(fat)
                .fiber(fib)
                .multiplier(multiplier)
                .build();
    }

    public CalculatedMacroResult calculateForUserFood(NutritionUserFood userFood, BigDecimal quantity, String unit) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        BigDecimal servingAmount = userFood.getServingAmount() != null && userFood.getServingAmount().compareTo(BigDecimal.ZERO) > 0
                ? userFood.getServingAmount() : new BigDecimal("100");
        String servingUnit = userFood.getServingUnit() != null ? userFood.getServingUnit().trim().toLowerCase() : "g";

        BigDecimal calculatedGrams = null;
        BigDecimal calculatedMl = null;
        BigDecimal multiplier;

        String normalizedUnit = unit != null ? unit.trim().toLowerCase() : "g";

        if (normalizedUnit.equals("g") || normalizedUnit.equals("gram") || normalizedUnit.equals("grams")) {
            calculatedGrams = quantity;
            multiplier = quantity.divide(servingAmount, 6, RoundingMode.HALF_UP);
        } else if (normalizedUnit.equals("kg") || normalizedUnit.equals("kilogram")) {
            calculatedGrams = quantity.multiply(new BigDecimal("1000"));
            multiplier = calculatedGrams.divide(servingAmount, 6, RoundingMode.HALF_UP);
        } else if (normalizedUnit.equals("ml") || normalizedUnit.equals("milliliter")) {
            calculatedMl = quantity;
            multiplier = quantity.divide(servingAmount, 6, RoundingMode.HALF_UP);
        } else if (normalizedUnit.equals("l") || normalizedUnit.equals("liter") || normalizedUnit.equals("lít")) {
            calculatedMl = quantity.multiply(new BigDecimal("1000"));
            multiplier = calculatedMl.divide(servingAmount, 6, RoundingMode.HALF_UP);
        } else if (normalizedUnit.equalsIgnoreCase(servingUnit)) {
            multiplier = quantity.divide(servingAmount, 6, RoundingMode.HALF_UP);
        } else {
            multiplier = quantity;
        }

        BigDecimal cal = userFood.getCalories() != null ? userFood.getCalories().multiply(multiplier).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal pro = userFood.getProtein() != null ? userFood.getProtein().multiply(multiplier).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal carb = userFood.getCarbohydrate() != null ? userFood.getCarbohydrate().multiply(multiplier).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal fat = userFood.getFat() != null ? userFood.getFat().multiply(multiplier).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal fib = userFood.getFiber() != null ? userFood.getFiber().multiply(multiplier).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return CalculatedMacroResult.builder()
                .calculatedGrams(calculatedGrams != null ? calculatedGrams.setScale(2, RoundingMode.HALF_UP) : null)
                .calculatedMl(calculatedMl != null ? calculatedMl.setScale(2, RoundingMode.HALF_UP) : null)
                .calories(cal)
                .protein(pro)
                .carbohydrate(carb)
                .fat(fat)
                .fiber(fib)
                .multiplier(multiplier)
                .build();
    }

    /**
     * Calculate NET activity calories burned using formula:
     * Net Calories = (MET - 1) * 3.5 * WeightKg / 200 * DurationMinutes
     */
    public BigDecimal calculateNetActivityCalories(BigDecimal metValue, BigDecimal weightKg, BigDecimal durationMinutes) {
        if (metValue == null || metValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (weightKg == null || weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cân nặng phải lớn hơn 0");
        }
        if (durationMinutes == null || durationMinutes.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Thời gian hoạt động phải lớn hơn 0");
        }

        // (MET - 1.0)
        BigDecimal metMinusOne = metValue.subtract(BigDecimal.ONE);
        if (metMinusOne.compareTo(BigDecimal.ZERO) < 0) {
            metMinusOne = BigDecimal.ZERO;
        }

        // (MET - 1) * 3.5 * WeightKg / 200 * DurationMinutes
        BigDecimal factor = metMinusOne.multiply(new BigDecimal("3.5")).multiply(weightKg);
        BigDecimal ratePerMinute = factor.divide(new BigDecimal("200"), 6, RoundingMode.HALF_UP);
        BigDecimal netCalories = ratePerMinute.multiply(durationMinutes);

        return netCalories.setScale(1, RoundingMode.HALF_UP);
    }
}
