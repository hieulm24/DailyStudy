package com.englishlearning.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakHeatmapDto {
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastStudyDate;
    private Long totalStudyDays;
    private List<HeatmapDayDto> heatmapDays;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HeatmapDayDto {
        private String date; // YYYY-MM-DD
        private Integer count;
        private Integer level; // 0, 1, 2, 3, 4 for heatmap color intensity
    }
}
