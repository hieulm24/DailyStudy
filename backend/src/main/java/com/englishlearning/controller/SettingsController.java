package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.settings.BackupDataDto;
import com.englishlearning.dto.settings.UserSettingDto;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.SettingsBackupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsBackupService settingsBackupService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserSettingDto>> getSettings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserSettingDto settings = settingsBackupService.getUserSettings(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserSettingDto>> updateSettings(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserSettingDto dto) {
        UserSettingDto settings = settingsBackupService.updateUserSettings(userPrincipal.getId(), dto);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật cài đặt thành công", settings));
    }

    @GetMapping("/export")
    public ResponseEntity<ApiResponse<BackupDataDto>> exportData(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BackupDataDto backup = settingsBackupService.exportData(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xuất dữ liệu thành công", backup));
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<Void>> importData(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody BackupDataDto backup) {
        settingsBackupService.importData(userPrincipal.getId(), backup);
        return ResponseEntity.ok(ApiResponse.ok("Nhập dữ liệu thành công", null));
    }
}
