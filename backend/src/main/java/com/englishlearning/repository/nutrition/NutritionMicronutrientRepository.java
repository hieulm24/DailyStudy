package com.englishlearning.repository.nutrition;

import com.englishlearning.entity.nutrition.NutritionMicronutrient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionMicronutrientRepository extends JpaRepository<NutritionMicronutrient, Long> {

    List<NutritionMicronutrient> findByVariantId(Long variantId);

    List<NutritionMicronutrient> findByVariantIdIn(List<Long> variantIds);
}
