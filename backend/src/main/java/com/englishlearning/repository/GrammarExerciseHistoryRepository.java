package com.englishlearning.repository;

import com.englishlearning.entity.GrammarExerciseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrammarExerciseHistoryRepository extends JpaRepository<GrammarExerciseHistory, Long> {

    List<GrammarExerciseHistory> findByUserIdAndGrammarTopicIdOrderByCompletedAtDesc(Long userId, Long grammarTopicId);

    Page<GrammarExerciseHistory> findByUserIdAndGrammarTopicId(Long userId, Long grammarTopicId, Pageable pageable);

    Optional<GrammarExerciseHistory> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndGrammarTopicId(Long userId, Long grammarTopicId);
}
