package com.englishlearning.dto.book;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookProgressRequest {

    @NotNull(message = "Số trang hiện tại không được để trống")
    private Integer currentPage;

    private String status; // WANT_TO_READ, READING, COMPLETED, ON_HOLD

    private Integer rating;

    private String reviewNotes;

    private Integer pagesReadToday; // Số trang đã đọc trong phiên này để log vào reading_logs

    private Integer minutesSpent;
}
