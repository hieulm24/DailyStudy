package com.englishlearning.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameQuestionOptionDto {
    private Long id;
    private String optionText;
    private Boolean isCorrect;
    private Integer displayOrder;
}
