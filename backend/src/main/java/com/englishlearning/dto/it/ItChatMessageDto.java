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
public class ItChatMessageDto {
    private Long id;
    private Long sessionId;
    private String senderRole;
    private String content;
    private String codeSnippet;
    private String language;
    private String mermaidDiagram;
    private String imageUrl;
    private LocalDateTime createdAt;
}
