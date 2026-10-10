package com.englishlearning.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewItemDto {
    private Long id;
    private String contentType; // VOCABULARY, GRAMMAR
    private Long contentId;
    private Integer masteryLevel;
    private Integer reviewCount;
    private Integer currentIntervalDays;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime nextReviewAt;

    // Content payload
    private String title; // word or topic
    private String subtitle; // pronunciation or level
    private String primaryMeaning; // meaning or usage
    private String structure; // grammar structure
    private String exampleSentence;
    private String exampleMeaning;
    private String contextSentence;
    private String contextMeaning;
    private String note;
}
