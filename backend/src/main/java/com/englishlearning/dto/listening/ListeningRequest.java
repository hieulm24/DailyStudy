package com.englishlearning.dto.listening;

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
public class ListeningRequest {
    @NotBlank(message = "Tiêu đề bài nghe không được để trống")
    private String title;

    private String description;
    private String url;
    private Integer durationSeconds;
    private String level;
    private String note;
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime learnedAt;
}
