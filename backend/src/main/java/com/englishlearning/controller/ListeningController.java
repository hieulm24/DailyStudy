package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.listening.ListeningFilterRequest;
import com.englishlearning.dto.listening.ListeningRequest;
import com.englishlearning.dto.listening.ListeningResponse;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.ListeningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listening")
@RequiredArgsConstructor
public class ListeningController {

    private final ListeningService listeningService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ListeningResponse>>> getListeningLessons(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute ListeningFilterRequest filter) {
        PageResponse<ListeningResponse> response = listeningService.getListeningLessons(userPrincipal.getId(), filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ListeningResponse>> getListeningLessonById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ListeningResponse response = listeningService.getListeningLessonById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ListeningResponse>> createListeningLesson(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ListeningRequest request) {
        ListeningResponse response = listeningService.createListeningLesson(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Ghi nhận bài nghe thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ListeningResponse>> updateListeningLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ListeningRequest request) {
        ListeningResponse response = listeningService.updateListeningLesson(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bài nghe thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteListeningLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        listeningService.deleteListeningLesson(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa bài nghe thành công", null));
    }
}
