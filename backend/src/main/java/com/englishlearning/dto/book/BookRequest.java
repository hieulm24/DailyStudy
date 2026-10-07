package com.englishlearning.dto.book;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @NotBlank(message = "Tên sách không được để trống")
    private String title;

    private String author;

    private String category;

    private String description;

    private Integer totalPages;

    private Integer currentPage;

    private String status; // WANT_TO_READ, READING, COMPLETED, ON_HOLD

    private Integer rating;

    private String reviewNotes;

    private LocalDateTime startDate;

    private LocalDateTime completedDate;

    private String coverUrl;
}
