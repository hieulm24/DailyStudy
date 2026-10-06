package com.englishlearning.repository.nutrition;

import com.englishlearning.entity.nutrition.NutritionFoodUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionFoodUnitRepository extends JpaRepository<NutritionFoodUnit, Long> {

    List<NutritionFoodUnit> findByFoodId(Long foodId);

    @Query("SELECT u FROM NutritionFoodUnit u WHERE u.food.id = :foodId AND LOWER(u.unit) = LOWER(:unit)")
    Optional<NutritionFoodUnit> findByFoodIdAndUnitIgnoreCase(@Param("foodId") Long foodId, @Param("unit") String unit);
}
