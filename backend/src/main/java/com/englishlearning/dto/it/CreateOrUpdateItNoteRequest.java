package com.englishlearning.dto.it;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateItNoteRequest {

    @NotBlank(message = "Tiêu đề ghi chú không được để trống")
    private String title;

    private String category;
    private String tags;

    @NotBlank(message = "Nội dung ghi chú không được để trống")
    private String contentMarkdown;

    private String diagramMermaid;
    private String imageUrlsJson;
    private Boolean isFavorite;
}
