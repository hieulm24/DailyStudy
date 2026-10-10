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
public class SendItChatRequest {

    private Long sessionId; // null if creating new session

    private String topicCategory;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String prompt;

    private String codeSnippet;
    private String language;
    private String imageUrl;
    private String apiKey;
}
