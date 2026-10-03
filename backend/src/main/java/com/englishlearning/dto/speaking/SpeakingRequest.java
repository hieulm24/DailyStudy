package com.englishlearning.dto.speaking;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingRequest {
    @NotBlank(message = "Tiêu đề bài nói không được để trống")
    private String title;

    private String topic;
    private String description;
    private String url;
    private Integer durationSeconds;
    private String level;
    private String note;
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime practicedAt;
}
