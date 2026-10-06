package com.englishlearning.repository.nutrition;

import com.englishlearning.entity.nutrition.NutritionActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionActivityLogRepository extends JpaRepository<NutritionActivityLog, Long> {

    @Query("SELECT l FROM NutritionActivityLog l JOIN FETCH l.activity WHERE l.userId = :userId AND l.logDate = :logDate ORDER BY l.createdAt ASC")
    List<NutritionActivityLog> findByUserIdAndLogDate(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    Optional<NutritionActivityLog> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT l FROM NutritionActivityLog l JOIN FETCH l.activity WHERE l.userId = :userId AND l.logDate BETWEEN :startDate AND :endDate ORDER BY l.logDate ASC, l.createdAt ASC")
    List<NutritionActivityLog> findByUserIdAndLogDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

