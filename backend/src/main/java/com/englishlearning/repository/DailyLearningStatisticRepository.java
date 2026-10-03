package com.englishlearning.repository;

import com.englishlearning.entity.DailyLearningStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyLearningStatisticRepository extends JpaRepository<DailyLearningStatistic, Long> {

    Optional<DailyLearningStatistic> findByUserIdAndStatisticDate(Long userId, LocalDate statisticDate);

    List<DailyLearningStatistic> findByUserIdAndStatisticDateBetweenOrderByStatisticDateAsc(Long userId, LocalDate startDate, LocalDate endDate);

    List<DailyLearningStatistic> findByUserIdOrderByStatisticDateDesc(Long userId);
}
