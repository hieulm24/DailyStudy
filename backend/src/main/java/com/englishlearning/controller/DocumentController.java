package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.document.DocumentFilterRequest;
import com.englishlearning.dto.document.DocumentResponse;
import com.englishlearning.dto.document.DocumentStatisticsResponse;
import com.englishlearning.dto.document.DocumentUpdateRequest;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "description", required = false) String description) {
        DocumentResponse response = documentService.uploadDocument(userPrincipal.getId(), file, title, category, description);
        return ResponseEntity.ok(ApiResponse.ok("Tải lên tài liệu thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DocumentResponse>>> getDocuments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute DocumentFilterRequest filter) {
        PageResponse<DocumentResponse> response = documentService.getDocuments(userPrincipal.getId(), filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DocumentResponse response = documentService.getDocumentById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DocumentResponse doc = documentService.getDocumentById(id, userPrincipal.getId());
        Resource resource = documentService.loadDocumentAsResource(id, userPrincipal.getId());

        String encodedFilename = URLEncoder.encode(doc.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        String contentDisposition = "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename;

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (doc.getMimeType() != null) {
            try {
                mediaType = MediaType.parseMediaType(doc.getMimeType());
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DocumentResponse doc = documentService.getDocumentById(id, userPrincipal.getId());
        Resource resource = documentService.loadDocumentAsResource(id, userPrincipal.getId());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (doc.getMimeType() != null) {
            try {
                mediaType = MediaType.parseMediaType(doc.getMimeType());
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DocumentUpdateRequest request) {
        DocumentResponse response = documentService.updateDocument(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin tài liệu thành công", response));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<DocumentResponse>> toggleFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DocumentResponse response = documentService.toggleFavorite(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        documentService.deleteDocument(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa tài liệu thành công", null));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<DocumentStatisticsResponse>> getStatistics(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DocumentStatisticsResponse response = documentService.getStatistics(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
