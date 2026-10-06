package com.englishlearning.repository.nutrition;

import com.englishlearning.entity.nutrition.NutritionActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionActivityRepository extends JpaRepository<NutritionActivity, Long> {

    List<NutritionActivity> findAllByIsActiveTrueOrderByNameAsc();

    List<NutritionActivity> findAllByIsActiveTrueOrderByCategoryAscNameAsc();

    Optional<NutritionActivity> findByName(String name);

    Optional<NutritionActivity> findFirstByNameIgnoreCase(String name);
}
