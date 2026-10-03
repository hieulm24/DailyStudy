package com.englishlearning.repository;

import com.englishlearning.entity.ReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Long> {

    List<ReviewHistory> findByUserId(Long userId);

    List<ReviewHistory> findByReviewItemIdOrderByReviewedAtDesc(Long reviewItemId);

    long countByUserId(Long userId);

    long countByUserIdAndReviewedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
