package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.studylink.StudyLinkFilterRequest;
import com.englishlearning.dto.studylink.StudyLinkRequest;
import com.englishlearning.dto.studylink.StudyLinkResponse;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.StudyLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study-links")
@RequiredArgsConstructor
public class StudyLinkController {

    private final StudyLinkService studyLinkService;

    @PostMapping
    public ResponseEntity<ApiResponse<StudyLinkResponse>> createLink(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StudyLinkRequest request) {
        StudyLinkResponse response = studyLinkService.createLink(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Lưu liên kết học tập thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StudyLinkResponse>>> getLinks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute StudyLinkFilterRequest filter) {
        PageResponse<StudyLinkResponse> response = studyLinkService.getLinks(userPrincipal.getId(), filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudyLinkResponse>> getLinkById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        StudyLinkResponse response = studyLinkService.getLinkById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudyLinkResponse>> updateLink(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StudyLinkRequest request) {
        StudyLinkResponse response = studyLinkService.updateLink(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật liên kết thành công", response));
    }

    @PatchMapping("/{id}/click")
    public ResponseEntity<ApiResponse<StudyLinkResponse>> recordClick(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        StudyLinkResponse response = studyLinkService.recordClick(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<StudyLinkResponse>> toggleFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        StudyLinkResponse response = studyLinkService.toggleFavorite(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLink(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        studyLinkService.deleteLink(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa liên kết thành công", null));
    }
}
