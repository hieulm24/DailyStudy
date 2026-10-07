package com.englishlearning.dto.book;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingLogRequest {

    private LocalDate logDate;

    @NotNull(message = "Số trang đọc không được để trống")
    private Integer pagesRead;

    private Integer minutesRead;

    private String note;
}
