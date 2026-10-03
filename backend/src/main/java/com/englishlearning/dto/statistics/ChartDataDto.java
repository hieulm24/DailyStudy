package com.englishlearning.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDto {
    private List<String> labels; // e.g. ["2026-09-20", "2026-09-21", ...]
    private List<Integer> vocabularyData;
    private List<Integer> grammarData;
    private List<Integer> listeningData;
    private List<Integer> speakingData;
    private List<Integer> reviewData;
    private List<Integer> totalData;
}
