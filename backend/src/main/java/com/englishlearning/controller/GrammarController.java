package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.grammar.GrammarFilterRequest;
import com.englishlearning.dto.grammar.GrammarRequest;
import com.englishlearning.dto.grammar.GrammarResponse;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.GrammarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grammar")
@RequiredArgsConstructor
public class GrammarController {

    private final GrammarService grammarService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GrammarResponse>>> getGrammars(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute GrammarFilterRequest filter) {
        PageResponse<GrammarResponse> response = grammarService.getGrammars(userPrincipal.getId(), filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GrammarResponse>> getGrammarById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        GrammarResponse response = grammarService.getGrammarById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GrammarResponse>> createGrammar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody GrammarRequest request) {
        GrammarResponse response = grammarService.createGrammar(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Thêm chủ đề ngữ pháp thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GrammarResponse>> updateGrammar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody GrammarRequest request) {
        GrammarResponse response = grammarService.updateGrammar(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật chủ đề ngữ pháp thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGrammar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        grammarService.deleteGrammar(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa chủ đề ngữ pháp thành công", null));
    }
}
