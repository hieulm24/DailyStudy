package com.englishlearning.dto.book;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingLogResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private LocalDate logDate;
    private Integer pagesRead;
    private Integer minutesRead;
    private String note;
    private LocalDateTime createdAt;
}
