package com.englishlearning.dto.studylink;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudyLinkRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Đường dẫn URL không được để trống")
    private String url;

    private String category;
    private String description;
    private Boolean isFavorite;
}
