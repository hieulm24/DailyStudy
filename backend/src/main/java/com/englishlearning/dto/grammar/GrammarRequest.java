package com.englishlearning.dto.grammar;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarRequest {
    @NotBlank(message = "Tên chủ đề ngữ pháp không được để trống")
    private String topic;

    private String level;
    private String structure;
    private String positiveStructure;
    private String negativeStructure;
    private String questionStructure;
    private String usage;
    private String signalWords;
    private String commonMistakes;
    private String note;
    private String status;

    // Quick primary example
    private String exampleSentence;
    private String exampleMeaning;

    // Multiple examples
    private List<GrammarExampleDto> examples;
}
