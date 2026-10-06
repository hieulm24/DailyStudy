package com.englishlearning.repository.nutrition;

import com.englishlearning.entity.nutrition.NutritionDailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionDailyLogRepository extends JpaRepository<NutritionDailyLog, Long> {

    List<NutritionDailyLog> findByUserIdAndLogDateOrderByCreatedAtAsc(Long userId, LocalDate logDate);

    Optional<NutritionDailyLog> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT d FROM NutritionDailyLog d WHERE d.userId = :userId AND d.logDate = :logDate ORDER BY d.id ASC")
    List<NutritionDailyLog> getLogsByDate(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    @Query("SELECT d FROM NutritionDailyLog d WHERE d.userId = :userId ORDER BY d.createdAt DESC")
    List<NutritionDailyLog> findRecentLogsByUserId(@Param("userId") Long userId);

    @Query("SELECT d FROM NutritionDailyLog d WHERE d.userId = :userId AND d.logDate BETWEEN :startDate AND :endDate ORDER BY d.logDate ASC, d.id ASC")
    List<NutritionDailyLog> findByUserIdAndLogDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

