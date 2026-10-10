package com.englishlearning.dto.it;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItPlaygroundExecutionResponse {
    private boolean success;
    private String stdout;
    private String stderr;
    private Long executionTimeMs;
    private String aiAnalysis;
    private String aiSuggestedCode;
    private String mermaidDiagram;
    private List<Map<String, Object>> sqlResultTable;
    private List<String> sqlColumns;
}
