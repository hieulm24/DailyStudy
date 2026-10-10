package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.auth.*;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserProfileDto profile = authService.getCurrentUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileDto updated = authService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin cá nhân thành công", updated));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công. Hãy ghi nhớ mật khẩu mới của bạn.", Map.of("status", "SUCCESS")));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        String otp = authService.sendForgotPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(
                "Mã OTP xác thực đã được gửi đến email của bạn.",
                Map.of("email", request.getEmail(), "devOtpPreview", otp)
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        boolean valid = authService.verifyOtp(request);
        if (!valid) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Mã OTP không hợp lệ hoặc đã hết hạn"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Xác thực OTP thành công", Map.of("valid", true)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(
            @Valid @RequestBody ResetPasswordWithOtpRequest request
    ) {
        authService.resetPasswordWithOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay bằng mật khẩu mới.", Map.of("status", "SUCCESS")));
    }

    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        String avatarUrl = authService.uploadAvatar(currentUser.getId(), file);
        return ResponseEntity.ok(ApiResponse.ok("Tải ảnh đại diện thành công", Map.of("avatarUrl", avatarUrl)));
    }

    @GetMapping("/avatars/{fileName:.+}")
    public ResponseEntity<Resource> getAvatarFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("uploads/avatars").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = "image/jpeg";
                if (fileName.toLowerCase().endsWith(".png")) contentType = "image/png";
                if (fileName.toLowerCase().endsWith(".webp")) contentType = "image/webp";
                if (fileName.toLowerCase().endsWith(".gif")) contentType = "image/gif";

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
