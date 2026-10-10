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
public class CreateOrUpdateSnippetRequest {

    @NotBlank(message = "Tiêu đề snippet không được để trống")
    private String title;

    private String language;

    @NotBlank(message = "Nội dung code không được để trống")
    private String codeContent;

    private String explanation;
    private String tags;
}
