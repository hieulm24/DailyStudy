package com.englishlearning.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicReviewSummaryDto {
    private Long topicId;
    private String topicName;
    private String level;
    private String status;
    private int totalWords;
    private int dueWords;
    private int masteredWords;
    private int learningWords;
}
