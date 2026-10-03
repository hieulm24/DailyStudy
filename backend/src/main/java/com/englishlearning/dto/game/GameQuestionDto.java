package com.englishlearning.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameQuestionDto {
    private Long id;
    private Long gameId;
    private String contentType;
    private Long contentId;
    private String questionText;
    private String prompt; // Secondary prompt if any
    private String targetAnswer; // For client-side checks/reference if needed
    private String explanation;
    private String difficulty;
    private List<GameQuestionOptionDto> options;
}
