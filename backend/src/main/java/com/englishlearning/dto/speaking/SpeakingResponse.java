package com.englishlearning.dto.speaking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingResponse {
    private Long id;
    private String title;
    private String topic;
    private String description;
    private String url;
    private Integer durationSeconds;
    private String level;
    private String note;
    private String status;
    private Integer practiceCount;
    private LocalDateTime practicedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
