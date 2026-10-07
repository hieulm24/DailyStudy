package com.englishlearning.dto.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyTopicResponse {
    private Long id;
    private String name;
    private String description;
    private String level;
    private String status;
    private Integer totalVocabularies;
    private Integer masteredCount;
    private Integer learningCount;
    private Integer newCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
