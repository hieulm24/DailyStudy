package com.englishlearning.repository;

import com.englishlearning.entity.GameQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameQuestionRepository extends JpaRepository<GameQuestion, Long> {
    List<GameQuestion> findByGameIdAndIsActiveTrue(Long gameId);
    List<GameQuestion> findByGameIdAndContentTypeAndContentId(Long gameId, String contentType, Long contentId);
}
