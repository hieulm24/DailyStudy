package com.englishlearning.repository;

import com.englishlearning.entity.GameAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameAnswerRepository extends JpaRepository<GameAnswer, Long> {
    List<GameAnswer> findByGameSessionId(Long gameSessionId);
}
