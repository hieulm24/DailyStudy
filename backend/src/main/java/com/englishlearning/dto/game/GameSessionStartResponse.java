package com.englishlearning.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionStartResponse {
    private Long sessionId;
    private Long gameId;
    private String gameCode;
    private String gameName;
    private Integer totalQuestions;
    private LocalDateTime startedAt;
    private List<GameQuestionDto> questions;
}
