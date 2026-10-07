package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.review.ReviewDueSummaryResponse;
import com.englishlearning.dto.review.ReviewItemDto;
import com.englishlearning.dto.review.ReviewSubmitRequest;
import com.englishlearning.dto.review.TopicReviewSummaryDto;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReviewDueSummaryResponse>> getReviewSummary(
            @RequestParam(required = false) Long topicId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ReviewDueSummaryResponse response = reviewService.getReviewSummary(userPrincipal.getId(), topicId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/topics-summary")
    public ResponseEntity<ApiResponse<List<TopicReviewSummaryDto>>> getTopicsReviewSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<TopicReviewSummaryDto> response = reviewService.getTopicsReviewSummary(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/due")
    public ResponseEntity<ApiResponse<List<ReviewItemDto>>> getDueReviewItems(
            @RequestParam(required = false) Long topicId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ReviewItemDto> response = reviewService.getDueReviewItems(userPrincipal.getId(), topicId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<ReviewItemDto>> submitReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReviewSubmitRequest request) {
        ReviewItemDto response = reviewService.submitReview(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật tiến độ ôn tập", response));
    }
}

