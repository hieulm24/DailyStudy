package com.englishlearning.repository;

import com.englishlearning.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    List<GameSession> findByUserIdOrderByStartedAtDesc(Long userId);
    long countByUserId(Long userId);
    long countByUserIdAndStartedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
