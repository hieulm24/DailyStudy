package com.englishlearning.dto.grammar;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class GrammarFilterRequest {
    private String search;
    private String level;
    private String status;
    private String dateRange; // TODAY, YESTERDAY, LAST_7_DAYS, LAST_30_DAYS, CUSTOM

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
