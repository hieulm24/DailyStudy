package com.englishlearning.dto.grammar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarExerciseHistoryResponse {
    private Long id;
    private Long grammarTopicId;
    private String topicName;
    private String title;
    private String exerciseType;
    private String level;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer score;
    private Integer timeSpentSeconds;
    private List<GrammarExerciseQuestionDto> questions;
    private Map<String, Object> userAnswers;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
