package com.englishlearning.repository;

import com.englishlearning.entity.ReadingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {

    List<ReadingLog> findByUserIdAndLogDateBetweenOrderByLogDateDesc(Long userId, LocalDate fromDate, LocalDate toDate);

    List<ReadingLog> findByBookIdOrderByLogDateDesc(Long bookId);

    @Query("SELECT SUM(r.pagesRead) FROM ReadingLog r WHERE r.user.id = :userId AND r.logDate = :date")
    Integer sumPagesReadByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT SUM(r.pagesRead) FROM ReadingLog r WHERE r.user.id = :userId AND r.logDate BETWEEN :fromDate AND :toDate")
    Integer sumPagesReadInRange(@Param("userId") Long userId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}
