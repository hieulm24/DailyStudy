package com.englishlearning.repository;

import com.englishlearning.entity.StudyStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyStreakRepository extends JpaRepository<StudyStreak, Long> {
    Optional<StudyStreak> findByUserId(Long userId);
}
