package com.englishlearning.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIExplanationResponse {
    private String summary;
    private String partOfSpeech;
    private String grammarPoint;
    private String contextUsage;
    private List<String> synonyms;
    private List<String> collocations;
    private List<ExamplePair> examples;
    private String rawExplanation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamplePair {
        private String en;
        private String vi;
    }
}
