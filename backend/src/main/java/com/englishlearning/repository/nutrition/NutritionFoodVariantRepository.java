package com.englishlearning.repository.nutrition;

import com.englishlearning.entity.nutrition.NutritionFoodVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionFoodVariantRepository extends JpaRepository<NutritionFoodVariant, Long> {

    List<NutritionFoodVariant> findByFoodId(Long foodId);

    @Query("SELECT v FROM NutritionFoodVariant v WHERE v.food.id = :foodId AND LOWER(v.state) = LOWER(:state)")
    Optional<NutritionFoodVariant> findByFoodIdAndStateIgnoreCase(@Param("foodId") Long foodId, @Param("state") String state);
}
