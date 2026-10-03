package com.englishlearning.dto.task;

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
public class DailyTaskSummaryResponse {
    private LocalDate taskDate;
    private String dayOfWeek;
    private String formattedDate;
    private Long totalTasks;
    private Long completedTasks;
    private Long pendingTasks;
    private Long inProgressTasks;
    private Double completionRate; // Percentage e.g. 80.0
    private String statusEvaluation; // EXCELLENT (100%), IN_PROGRESS, NOT_STARTED
    private List<DailyTaskResponse> tasks;
}
