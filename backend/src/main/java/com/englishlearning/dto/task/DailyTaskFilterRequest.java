package com.englishlearning.dto.task;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class DailyTaskFilterRequest {
    private String search;
    private String category;
    private String priority;
    private String status;
    private Boolean isCompleted;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate taskDate;

    private String dateRange;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private Integer page = 0;
    private Integer size = 50;
    private String sortBy = "displayOrder";
    private String sortDirection = "ASC";
}
