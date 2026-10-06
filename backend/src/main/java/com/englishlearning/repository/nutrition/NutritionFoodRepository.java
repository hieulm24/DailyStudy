package com.englishlearning.repository.nutrition;

import com.englishlearning.entity.nutrition.NutritionFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionFoodRepository extends JpaRepository<NutritionFood, Long> {

    List<NutritionFood> findAllByIsActiveTrueOrderByNameAsc();

    java.util.Optional<NutritionFood> findByName(String name);

    java.util.Optional<NutritionFood> findFirstByNameIgnoreCase(String name);

    @Query("SELECT f FROM NutritionFood f WHERE f.isActive = true AND LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY f.name ASC")
    List<NutritionFood> searchByName(@Param("keyword") String keyword);
}
