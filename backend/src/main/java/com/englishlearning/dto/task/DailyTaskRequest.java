package com.englishlearning.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class DailyTaskRequest {

    @NotNull(message = "Ngày thực hiện không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate taskDate;

    @NotBlank(message = "Tên công việc không được để trống")
    private String title;

    private String description;
    private String category; // ENGLISH, WORK, PERSONAL, PROJECT, OTHER
    private String priority = "MEDIUM"; // HIGH, MEDIUM, LOW
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    private Boolean isCompleted;
    private Integer displayOrder;
    private String estimatedTime;
}
