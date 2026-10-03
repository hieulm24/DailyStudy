package com.englishlearning.repository;

import com.englishlearning.entity.SpeakingLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpeakingLessonRepository extends JpaRepository<SpeakingLesson, Long>, JpaSpecificationExecutor<SpeakingLesson> {

    List<SpeakingLesson> findByUserId(Long userId);

    Optional<SpeakingLesson> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
