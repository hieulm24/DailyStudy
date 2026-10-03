package com.englishlearning.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDueSummaryResponse {
    private long totalDue;
    private long vocabularyDue;
    private long grammarDue;
    private long totalItems;
    private long totalMastered;
}
