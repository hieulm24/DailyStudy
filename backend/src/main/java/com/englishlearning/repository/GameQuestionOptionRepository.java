package com.englishlearning.repository;

import com.englishlearning.entity.GameQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameQuestionOptionRepository extends JpaRepository<GameQuestionOption, Long> {
    List<GameQuestionOption> findByQuestionIdOrderByDisplayOrderAsc(Long questionId);
}
