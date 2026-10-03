package com.englishlearning.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIExplanationRequest {
    private String text;
    private String contextSentence;
    private String apiKey;
}
