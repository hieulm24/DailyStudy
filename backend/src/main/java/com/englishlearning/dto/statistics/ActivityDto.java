package com.englishlearning.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDto {
    private Long id;
    private String activityType; // ADD_VOCABULARY, LEARN_GRAMMAR, LISTEN, SPEAK, REVIEW, PLAY_GAME
    private String contentType;
    private Long contentId;
    private String title;
    private String description;
    private LocalDateTime activityDate;
    private Integer durationSeconds;
}
