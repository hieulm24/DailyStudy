package com.englishlearning.dto.it;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItAiResponseDto {
    private String answerMarkdown;
    private String mermaidDiagram;
    private String optimizedCode;
    private String language;
    private String keyTakeaways;
}
