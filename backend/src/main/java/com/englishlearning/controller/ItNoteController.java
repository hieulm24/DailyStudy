package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.it.CreateOrUpdateItNoteRequest;
import com.englishlearning.dto.it.ItNoteDto;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.ItNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/it/notes")
@RequiredArgsConstructor
public class ItNoteController {

    private final ItNoteService noteService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItNoteDto>>> getNotes(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        PageResponse<ItNoteDto> result = noteService.getNotes(userPrincipal.getId(), category, search, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItNoteDto>> getNoteById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        ItNoteDto dto = noteService.getNoteById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItNoteDto>> createNote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateOrUpdateItNoteRequest request
    ) {
        ItNoteDto dto = noteService.createNote(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Tạo ghi chú IT thành công", dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItNoteDto>> updateNote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody CreateOrUpdateItNoteRequest request
    ) {
        ItNoteDto dto = noteService.updateNote(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật ghi chú IT thành công", dto));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<ItNoteDto>> toggleFavorite(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        ItNoteDto dto = noteService.toggleFavorite(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> uploadNoteImage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) {
        String fileUrl = noteService.uploadNoteImage(userPrincipal.getId(), file);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("url", fileUrl);
        return ResponseEntity.ok(ApiResponse.ok("Tải ảnh ghi chú thành công", response));
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> getNoteImageFile(@PathVariable String filename) {
        org.springframework.core.io.Resource resource = noteService.loadNoteImageFile(filename);
        String contentType = "image/jpeg";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            contentType = "image/png";
        } else if (lower.endsWith(".webp")) {
            contentType = "image/webp";
        } else if (lower.endsWith(".svg")) {
            contentType = "image/svg+xml";
        } else if (lower.endsWith(".gif")) {
            contentType = "image/gif";
        }

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        noteService.deleteNote(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>ok("Đã xóa ghi chú thành công", null));
    }
}
