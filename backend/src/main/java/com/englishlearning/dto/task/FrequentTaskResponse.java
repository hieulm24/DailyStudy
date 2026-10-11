package com.englishlearning.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequentTaskResponse {
    private String title;
    private String category;
    private String priority;
    private String estimatedTime;
    private String description;
    private Long frequency;
}
