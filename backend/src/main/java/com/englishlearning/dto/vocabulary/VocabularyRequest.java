package com.englishlearning.dto.vocabulary;

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
public class VocabularyRequest {
    @NotBlank(message = "Từ vựng không được để trống")
    private String word;

    @NotBlank(message = "Nghĩa không được để trống")
    private String meaning;

    private String pronunciation;
    private String partOfSpeech;
    private String level;
    private String note;
    private String status;
    private Long topicId;

    // Example sentence
    private String exampleSentence;
    private String exampleMeaning;

    // Multiple examples if provided
    private List<VocabularyExampleDto> examples;
}
