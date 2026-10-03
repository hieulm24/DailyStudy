package com.englishlearning.dto.grammar;

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
public class GrammarResponse {
    private Long id;
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
    private Integer masteryLevel;
    private Integer reviewCount;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime nextReviewAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<GrammarExampleDto> examples;
}
