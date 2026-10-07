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
public class VocabularyTopicRequest {
    @NotBlank(message = "Tên chủ đề không được để trống")
    private String name;

    private String description;
    private String level;
    private String status;

    private List<VocabularyRequest> vocabularies;
}
