package com.englishlearning.dto.game;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAnswerSubmitRequest {
    @NotNull(message = "Session ID không được để trống")
    private Long sessionId;

    private List<SingleAnswerDto> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SingleAnswerDto {
        private Long questionId;
        private Long selectedOptionId;
        private String answerText;
        private Boolean isCorrect;
    }
}
