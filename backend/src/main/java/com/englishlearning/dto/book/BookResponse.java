package com.englishlearning.dto.book;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

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
    private Integer quoteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
