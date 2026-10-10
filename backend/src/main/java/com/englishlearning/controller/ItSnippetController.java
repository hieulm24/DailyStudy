package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.it.CreateOrUpdateSnippetRequest;
import com.englishlearning.dto.it.ItCodeSnippetDto;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.ItSnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/it/snippets")
@RequiredArgsConstructor
public class ItSnippetController {

    private final ItSnippetService snippetService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItCodeSnippetDto>>> getSnippets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        PageResponse<ItCodeSnippetDto> res = snippetService.getSnippets(userPrincipal.getId(), language, search, page, size);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItCodeSnippetDto>> getSnippetById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        ItCodeSnippetDto dto = snippetService.getSnippetById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItCodeSnippetDto>> createSnippet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateOrUpdateSnippetRequest request
    ) {
        ItCodeSnippetDto dto = snippetService.createSnippet(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Lưu code snippet thành công", dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItCodeSnippetDto>> updateSnippet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody CreateOrUpdateSnippetRequest request
    ) {
        ItCodeSnippetDto dto = snippetService.updateSnippet(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật code snippet thành công", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSnippet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        snippetService.deleteSnippet(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>ok("Đã xóa code snippet", null));
    }
}
