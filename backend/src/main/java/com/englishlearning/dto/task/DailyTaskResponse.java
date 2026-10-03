package com.englishlearning.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTaskResponse {
    private Long id;
    private LocalDate taskDate;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private Boolean isCompleted;
    private Integer displayOrder;
    private String estimatedTime;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
