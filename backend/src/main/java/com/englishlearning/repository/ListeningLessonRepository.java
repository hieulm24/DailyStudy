package com.englishlearning.repository;

import com.englishlearning.entity.ListeningLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListeningLessonRepository extends JpaRepository<ListeningLesson, Long>, JpaSpecificationExecutor<ListeningLesson> {

    List<ListeningLesson> findByUserId(Long userId);

    Optional<ListeningLesson> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
