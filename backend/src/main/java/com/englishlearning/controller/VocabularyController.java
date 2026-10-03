package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.vocabulary.VocabularyFilterRequest;
import com.englishlearning.dto.vocabulary.VocabularyRequest;
import com.englishlearning.dto.vocabulary.VocabularyResponse;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.VocabularyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VocabularyResponse>>> getVocabularies(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute VocabularyFilterRequest filter) {
        PageResponse<VocabularyResponse> response = vocabularyService.getVocabularies(userPrincipal.getId(), filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyResponse>> getVocabularyById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        VocabularyResponse response = vocabularyService.getVocabularyById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VocabularyResponse>> createVocabulary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody VocabularyRequest request) {
        VocabularyResponse response = vocabularyService.createVocabulary(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Thêm từ vựng thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyResponse>> updateVocabulary(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody VocabularyRequest request) {
        VocabularyResponse response = vocabularyService.updateVocabulary(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật từ vựng thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVocabulary(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        vocabularyService.deleteVocabulary(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa từ vựng thành công", null));
    }

    @PostMapping("/{id}/mastered")
    public ResponseEntity<ApiResponse<VocabularyResponse>> markAsMastered(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        VocabularyResponse response = vocabularyService.markAsMastered(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Đã đánh dấu thuộc lòng từ vựng", response));
    }
}
