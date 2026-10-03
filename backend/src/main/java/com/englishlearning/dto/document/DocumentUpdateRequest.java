package com.englishlearning.dto.document;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentUpdateRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    private String category;
    private String description;
    private Boolean isFavorite;
}
