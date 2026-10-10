package com.englishlearning.dto.grammar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateGrammarExerciseRequest {
    private Long topicId;
    private Integer numberOfQuestions; // default 5, 10
    private String exerciseType; // MULTIPLE_CHOICE, FILL_IN_BLANK, ERROR_CORRECTION, MIXED
    private String level; // A1, A2, B1, B2, C1
    private String customFocus; // Ví dụ: "Tập trung phân biệt thì Hiện tại hoàn thành và Quá khứ đơn"
}
