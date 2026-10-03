package com.englishlearning.dto.listening;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListeningResponse {
    private Long id;
    private String title;
    private String description;
    private String url;
    private Integer durationSeconds;
    private String level;
    private String note;
    private String status;
    private Integer listenedCount;
    private LocalDateTime learnedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
