package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.ai.AIExplanationRequest;
import com.englishlearning.dto.ai.AIExplanationResponse;
import com.englishlearning.dto.ai.TranslationResponse;
import com.englishlearning.service.AiTranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiTranslationController {

    private final AiTranslationService aiTranslationService;

    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<TranslationResponse>> lookup(@RequestParam("q") String query) {
        TranslationResponse response = aiTranslationService.translateAndLookup(query);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/explain")
    public ResponseEntity<ApiResponse<AIExplanationResponse>> explain(@RequestBody AIExplanationRequest request) {
        AIExplanationResponse response = aiTranslationService.explainWithAI(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
