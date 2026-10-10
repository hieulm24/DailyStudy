package com.englishlearning.dto.it;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItNoteDto {
    private Long id;
    private String title;
    private String category;
    private String tags;
    private String contentMarkdown;
    private String diagramMermaid;
    private String imageUrlsJson;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
