package com.englishlearning.dto.book;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingStatsResponse {

    private long totalBooks;
    private long completedBooks;
    private long readingBooks;
    private long wantToReadBooks;
    private long onHoldBooks;
    private long totalPagesRead;
    private long totalQuotes;
    private long favoriteQuotes;
    private List<String> categories;
    private Map<String, Long> booksByCategory;
    private List<BookResponse> recentBooks;
}
