package com.englishlearning.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResultResponse {
    private Long sessionId;
    private String gameCode;
    private String gameName;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private BigDecimal score; // 0 - 100 percentage
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
