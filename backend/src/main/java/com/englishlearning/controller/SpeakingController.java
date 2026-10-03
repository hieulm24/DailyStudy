package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.speaking.SpeakingFilterRequest;
import com.englishlearning.dto.speaking.SpeakingRequest;
import com.englishlearning.dto.speaking.SpeakingResponse;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.SpeakingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/speaking")
@RequiredArgsConstructor
public class SpeakingController {

    private final SpeakingService speakingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SpeakingResponse>>> getSpeakingLessons(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute SpeakingFilterRequest filter) {
        PageResponse<SpeakingResponse> response = speakingService.getSpeakingLessons(userPrincipal.getId(), filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpeakingResponse>> getSpeakingLessonById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SpeakingResponse response = speakingService.getSpeakingLessonById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SpeakingResponse>> createSpeakingLesson(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SpeakingRequest request) {
        SpeakingResponse response = speakingService.createSpeakingLesson(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Ghi nhận bài luyện nói thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SpeakingResponse>> updateSpeakingLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SpeakingRequest request) {
        SpeakingResponse response = speakingService.updateSpeakingLesson(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bài luyện nói thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpeakingLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        speakingService.deleteSpeakingLesson(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa bài luyện nói thành công", null));
    }
}
