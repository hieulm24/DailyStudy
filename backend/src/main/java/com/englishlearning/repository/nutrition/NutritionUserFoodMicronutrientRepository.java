package com.englishlearning.repository.nutrition;

import com.englishlearning.entity.nutrition.NutritionUserFoodMicronutrient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionUserFoodMicronutrientRepository extends JpaRepository<NutritionUserFoodMicronutrient, Long> {

    List<NutritionUserFoodMicronutrient> findByUserFoodId(Long userFoodId);

    List<NutritionUserFoodMicronutrient> findByUserFoodIdIn(List<Long> userFoodIds);
}
