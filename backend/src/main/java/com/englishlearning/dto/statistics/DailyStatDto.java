package com.englishlearning.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatDto {
    private LocalDate date;
    private Integer vocabularyCount;
    private Integer grammarCount;
    private Integer listeningCount;
    private Integer speakingCount;
    private Integer reviewCount;
    private Integer gameCount;
    private Integer totalLearningCount;
    private Integer totalLearningSeconds;
}
