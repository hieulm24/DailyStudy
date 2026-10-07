package com.englishlearning.dto.book;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDetailResponse {

    private Long id;
    private Long userId;
    private String title;
    private String author;
    private String category;
    private String description;
    private Integer totalPages;
    private Integer currentPage;
    private Double progressPercentage;
    private String status;
    private Integer rating;
    private String reviewNotes;
    private LocalDateTime startDate;
    private LocalDateTime completedDate;
    private String coverUrl;
    private List<BookQuoteResponse> quotes;
    private List<ReadingLogResponse> readingLogs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
