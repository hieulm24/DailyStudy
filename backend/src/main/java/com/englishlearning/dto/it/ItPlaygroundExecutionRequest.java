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
public class ItPlaygroundExecutionRequest {

    @NotBlank(message = "Ngôn ngữ không được để trống")
    private String language; // "JAVASCRIPT", "TYPESCRIPT", "SQL", "JAVA", "PYTHON"

    @NotBlank(message = "Nội dung code không được để trống")
    private String code;

    private String stdin;
    private String action; // "RUN", "DEBUG_AI", "OPTIMIZE_AI", "EXPLAIN_AI"
    private String apiKey;
}
