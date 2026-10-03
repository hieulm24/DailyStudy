package com.englishlearning.dto.vocabulary;

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
public class VocabularyResponse {
    private Long id;
    private String word;
    private String meaning;
    private String pronunciation;
    private String partOfSpeech;
    private String level;
    private String note;
    private String status;
    private Integer masteryLevel;
    private Integer reviewCount;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime nextReviewAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<VocabularyExampleDto> examples;
}
