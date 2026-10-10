package com.englishlearning.service;

import com.englishlearning.dto.it.ItAiResponseDto;
import com.englishlearning.dto.it.ItPlaygroundExecutionRequest;
import com.englishlearning.dto.it.ItPlaygroundExecutionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItPlaygroundService {

    private final ItAiService itAiService;

    public ItPlaygroundExecutionResponse executeOrAnalyze(ItPlaygroundExecutionRequest request) {
        long startTime = System.currentTimeMillis();
        String action = request.getAction() != null ? request.getAction().toUpperCase() : "RUN";
        String lang = request.getLanguage() != null ? request.getLanguage().toUpperCase() : "JAVASCRIPT";

        if ("DEBUG_AI".equals(action) || "OPTIMIZE_AI".equals(action) || "EXPLAIN_AI".equals(action)) {
            ItAiResponseDto aiRes = itAiService.debugOrOptimizeCode(lang, request.getCode(), action, request.getApiKey());
            long elapsed = System.currentTimeMillis() - startTime;

            return ItPlaygroundExecutionResponse.builder()
                    .success(true)
                    .executionTimeMs(elapsed)
                    .aiAnalysis(aiRes.getAnswerMarkdown())
                    .aiSuggestedCode(aiRes.getOptimizedCode())
                    .mermaidDiagram(aiRes.getMermaidDiagram())
                    .build();
        }

        // Action == RUN
        if ("SQL".equals(lang)) {
            // Simulated SQL result generator or query analyzer
            List<String> cols = List.of("id", "username", "role", "total_orders", "status", "created_at");
            List<Map<String, Object>> rows = new ArrayList<>();

            Map<String, Object> r1 = new HashMap<>();
            r1.put("id", 101);
            r1.put("username", "minhhieu.dev");
            r1.put("role", "SENIOR_DEV");
            r1.put("total_orders", 24);
            r1.put("status", "ACTIVE");
            r1.put("created_at", "2026-10-01 08:30:00");
            rows.add(r1);

            Map<String, Object> r2 = new HashMap<>();
            r2.put("id", 102);
            r2.put("username", "architect.lead");
            r2.put("role", "SYSTEM_ARCHITECT");
            r2.put("total_orders", 58);
            r2.put("status", "ACTIVE");
            r2.put("created_at", "2026-09-15 14:15:20");
            rows.add(r2);

            long elapsed = System.currentTimeMillis() - startTime;
            return ItPlaygroundExecutionResponse.builder()
                    .success(true)
                    .stdout("Query executed successfully! 2 rows returned.")
                    .executionTimeMs(elapsed)
                    .sqlColumns(cols)
                    .sqlResultTable(rows)
                    .aiAnalysis("Câu lệnh SQL hợp lệ. Đã quét theo Index Seek.")
                    .build();
        }

        // Generic Code Run / Simulation with AI feedback
        ItAiResponseDto aiRes = itAiService.askArchitect(
                "Chạy mô phỏng code này, đưa ra kết quả in ra màn hình (stdout), phân tích lỗi runtime nếu có.",
                request.getCode(),
                lang,
                "CODE_RUNNER",
                request.getApiKey()
        );
        long elapsed = System.currentTimeMillis() - startTime;

        return ItPlaygroundExecutionResponse.builder()
                .success(true)
                .stdout("Execution simulated successfully.\nOutput:\n" + (aiRes.getOptimizedCode() != null ? aiRes.getOptimizedCode() : "Completed with exit code 0."))
                .executionTimeMs(elapsed)
                .aiAnalysis(aiRes.getAnswerMarkdown())
                .mermaidDiagram(aiRes.getMermaidDiagram())
                .build();
    }
}
