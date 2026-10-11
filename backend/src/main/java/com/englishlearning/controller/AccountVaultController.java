package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.vault.*;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.vault.AccountVaultService;
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
import java.util.List;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class AccountVaultController {

    private final AccountVaultService vaultService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<AccountCategoryDto>>> getCategories(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<AccountCategoryDto> categories = vaultService.getCategories(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @GetMapping("/credentials")
    public ResponseEntity<ApiResponse<PageResponse<AccountCredentialDto>>> getCredentials(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isFavorite,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        PageResponse<AccountCredentialDto> result = vaultService.getCredentials(
                userPrincipal.getId(),
                categoryId,
                isFavorite,
                isDeleted,
                search,
                page,
                size
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/credentials/active-all")
    public ResponseEntity<ApiResponse<List<AccountCredentialDto>>> getAllActiveCredentials(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<AccountCredentialDto> list = vaultService.getAllActiveCredentials(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/credentials/{id}")
    public ResponseEntity<ApiResponse<AccountCredentialDetailDto>> getCredentialDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        AccountCredentialDetailDto detail = vaultService.getCredentialDetail(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    @PostMapping("/credentials")
    public ResponseEntity<ApiResponse<AccountCredentialDetailDto>> createCredential(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateAccountCredentialRequest request
    ) {
        AccountCredentialDetailDto created = vaultService.createCredential(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đã thêm tài khoản vào Két bảo mật thành công", created));
    }

    @PutMapping("/credentials/{id}")
    public ResponseEntity<ApiResponse<AccountCredentialDetailDto>> updateCredential(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountCredentialRequest request
    ) {
        AccountCredentialDetailDto updated = vaultService.updateCredential(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật thông tin tài khoản thành công", updated));
    }

    @PatchMapping("/credentials/{id}/favorite")
    public ResponseEntity<ApiResponse<Void>> toggleFavorite(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        vaultService.toggleFavorite(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật trạng thái yêu thích", null));
    }

    @PatchMapping("/credentials/{id}/trash")
    public ResponseEntity<ApiResponse<Void>> moveToTrash(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        vaultService.moveToTrash(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Đã chuyển tài khoản vào thùng rác", null));
    }

    @PatchMapping("/credentials/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreFromTrash(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        vaultService.restoreFromTrash(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Đã khôi phục tài khoản thành công", null));
    }

    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePermanently(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        vaultService.deletePermanently(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa vĩnh viễn tài khoản khỏi hệ thống", null));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<VaultSummaryDto>> getSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        VaultSummaryDto summary = vaultService.getSummary(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PostMapping("/generator")
    public ResponseEntity<ApiResponse<PasswordGeneratorResponse>> generatePassword(
            @RequestBody PasswordGeneratorRequest request
    ) {
        PasswordGeneratorResponse res = vaultService.generatePassword(request);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PostMapping(value = "/credentials/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AccountAttachmentDto>> uploadAttachment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description
    ) {
        AccountAttachmentDto attachment = vaultService.uploadAttachment(userPrincipal.getId(), id, file, description);
        return ResponseEntity.ok(ApiResponse.ok("Tải lên tệp đính kèm thành công", attachment));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long attachmentId
    ) {
        vaultService.deleteAttachment(userPrincipal.getId(), attachmentId);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa tệp đính kèm", null));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long attachmentId
    ) {
        AccountAttachmentDto meta = vaultService.getAttachmentMetadata(userPrincipal.getId(), attachmentId);
        Resource resource = vaultService.loadAttachmentResource(userPrincipal.getId(), attachmentId);

        String encodedFilename = URLEncoder.encode(meta.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        String contentDisposition = "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename;

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (meta.getMimeType() != null) {
            try {
                mediaType = MediaType.parseMediaType(meta.getMimeType());
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    @GetMapping("/attachments/{attachmentId}/preview")
    public ResponseEntity<Resource> previewAttachment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long attachmentId
    ) {
        AccountAttachmentDto meta = vaultService.getAttachmentMetadata(userPrincipal.getId(), attachmentId);
        Resource resource = vaultService.loadAttachmentResource(userPrincipal.getId(), attachmentId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (meta.getMimeType() != null) {
            try {
                mediaType = MediaType.parseMediaType(meta.getMimeType());
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }
}
