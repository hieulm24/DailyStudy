package com.englishlearning.repository;

import com.englishlearning.entity.DailyTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, Long>, JpaSpecificationExecutor<DailyTask> {

    List<DailyTask> findByUserIdAndTaskDateOrderByDisplayOrderAscCreatedAtAsc(Long userId, LocalDate taskDate);

    Optional<DailyTask> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndTaskDate(Long userId, LocalDate taskDate);

    long countByUserIdAndTaskDateAndIsCompletedTrue(Long userId, LocalDate taskDate);

    long countByUserId(Long userId);

    long countByUserIdAndIsCompletedTrue(Long userId);

    @Query("SELECT DISTINCT d.taskDate FROM DailyTask d WHERE d.user.id = :userId ORDER BY d.taskDate DESC")
    Page<LocalDate> findDistinctTaskDatesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT d.taskDate FROM DailyTask d WHERE d.user.id = :userId AND d.taskDate BETWEEN :fromDate AND :toDate ORDER BY d.taskDate DESC")
    Page<LocalDate> findDistinctTaskDatesByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(MAX(d.displayOrder), 0) FROM DailyTask d WHERE d.user.id = :userId AND d.taskDate = :taskDate")
    Integer findMaxDisplayOrderByUserIdAndTaskDate(@Param("userId") Long userId, @Param("taskDate") LocalDate taskDate);
}
