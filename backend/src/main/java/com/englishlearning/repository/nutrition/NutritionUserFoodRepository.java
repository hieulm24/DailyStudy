package com.englishlearning.repository.nutrition;

import com.englishlearning.entity.nutrition.NutritionUserFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionUserFoodRepository extends JpaRepository<NutritionUserFood, Long> {

    List<NutritionUserFood> findByUserIdOrderByNameAsc(Long userId);

    @Query("SELECT u FROM NutritionUserFood u WHERE u.userId = :userId AND LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY u.name ASC")
    List<NutritionUserFood> searchByUserIdAndName(@Param("userId") Long userId, @Param("keyword") String keyword);

    Optional<NutritionUserFood> findByIdAndUserId(Long id, Long userId);
}
