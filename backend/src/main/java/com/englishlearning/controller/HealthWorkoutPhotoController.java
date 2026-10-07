package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.health.HealthWorkoutPhotoRequest;
import com.englishlearning.dto.health.HealthWorkoutPhotoResponse;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.HealthWorkoutPhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/health/photos")
@RequiredArgsConstructor
public class HealthWorkoutPhotoController {

    private final HealthWorkoutPhotoService photoService;

    @PostMapping
    public ResponseEntity<ApiResponse<HealthWorkoutPhotoResponse>> savePhoto(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody HealthWorkoutPhotoRequest request) {
        HealthWorkoutPhotoResponse response = photoService.savePhoto(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Lưu ảnh tập luyện thành công", response));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<HealthWorkoutPhotoResponse>> uploadPhoto(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "logDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate logDate,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "weightKg", required = false) BigDecimal weightKg) {
        HealthWorkoutPhotoResponse response = photoService.uploadAndSavePhoto(userPrincipal.getId(), file, logDate, caption, weightKg);
        return ResponseEntity.ok(ApiResponse.ok("Tải ảnh tập luyện thành công", response));
    }

    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<List<HealthWorkoutPhotoResponse>>> getPhotosByDate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<HealthWorkoutPhotoResponse> response = photoService.getPhotosByDate(userPrincipal.getId(), date);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<HealthWorkoutPhotoResponse>>> getAllPhotos(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        PageResponse<HealthWorkoutPhotoResponse> response = photoService.getAllPhotos(userPrincipal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        photoService.deletePhoto(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa ảnh tập luyện thành công", null));
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getPhotoFile(@PathVariable String filename) {
        Resource resource = photoService.loadPhotoFile(filename);
        String contentType = "image/jpeg";
        if (filename.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        } else if (filename.toLowerCase().endsWith(".webp")) {
            contentType = "image/webp";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
