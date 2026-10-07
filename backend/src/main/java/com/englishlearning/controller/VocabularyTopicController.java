package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.vocabulary.VocabularyTopicFilterRequest;
import com.englishlearning.dto.vocabulary.VocabularyTopicRequest;
import com.englishlearning.dto.vocabulary.VocabularyTopicResponse;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.VocabularyTopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vocabulary-topics")
@RequiredArgsConstructor
public class VocabularyTopicController {

    private final VocabularyTopicService vocabularyTopicService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VocabularyTopicResponse>>> getTopics(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute VocabularyTopicFilterRequest filter) {
        PageResponse<VocabularyTopicResponse> response = vocabularyTopicService.getTopics(userPrincipal.getId(), filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<VocabularyTopicResponse>>> getAllTopics(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<VocabularyTopicResponse> response = vocabularyTopicService.getAllTopics(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyTopicResponse>> getTopicById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        VocabularyTopicResponse response = vocabularyTopicService.getTopicById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VocabularyTopicResponse>> createTopic(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody VocabularyTopicRequest request) {
        VocabularyTopicResponse response = vocabularyTopicService.createTopic(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Thêm chủ đề thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyTopicResponse>> updateTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody VocabularyTopicRequest request) {
        VocabularyTopicResponse response = vocabularyTopicService.updateTopic(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật chủ đề thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        vocabularyTopicService.deleteTopic(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa chủ đề thành công", null));
    }
}
