package com.englishlearning.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTaskStatsResponse {
    private Long todayTotal;
    private Long todayCompleted;
    private Long todayPending;
    private Long todayInProgress;
    private Double todayCompletionRate;

    private Long totalTasksAllTime;
    private Long totalCompletedAllTime;
    private Double overallCompletionRate;

    private Long totalDaysWithTasks;
    private Long totalDaysCompletedFull; // Days with 100% completion

    private Map<String, Long> countByCategory;
    private Map<String, Long> countByPriority;
}
