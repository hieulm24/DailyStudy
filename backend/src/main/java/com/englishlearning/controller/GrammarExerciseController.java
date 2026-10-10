package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.grammar.GenerateGrammarExerciseRequest;
import com.englishlearning.dto.grammar.GrammarExerciseHistoryResponse;
import com.englishlearning.dto.grammar.GrammarExerciseQuestionDto;
import com.englishlearning.dto.grammar.SubmitGrammarExerciseRequest;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.GrammarAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grammar/exercises")
@RequiredArgsConstructor
public class GrammarExerciseController {

    private final GrammarAiService grammarAiService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<GrammarExerciseQuestionDto>>> generateExercises(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody GenerateGrammarExerciseRequest request) {
        List<GrammarExerciseQuestionDto> questions = grammarAiService.generateExercises(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Tạo bài tập ngữ pháp thành công", questions));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<GrammarExerciseHistoryResponse>> submitExercise(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SubmitGrammarExerciseRequest request) {
        GrammarExerciseHistoryResponse response = grammarAiService.submitExercise(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đã nộp bài và lưu lịch sử thành công", response));
    }

    @GetMapping("/history/topic/{topicId}")
    public ResponseEntity<ApiResponse<List<GrammarExerciseHistoryResponse>>> getTopicHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long topicId) {
        List<GrammarExerciseHistoryResponse> list = grammarAiService.getTopicHistory(userPrincipal.getId(), topicId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/history/{historyId}")
    public ResponseEntity<ApiResponse<GrammarExerciseHistoryResponse>> getHistoryDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long historyId) {
        GrammarExerciseHistoryResponse response = grammarAiService.getHistoryDetail(userPrincipal.getId(), historyId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
