package com.englishlearning.service;

import com.englishlearning.dto.auth.*;
import com.englishlearning.entity.User;
import com.englishlearning.entity.UserSetting;
import com.englishlearning.repository.*;
import com.englishlearning.security.JwtTokenProvider;
import com.englishlearning.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailOtpService emailOtpService;
    private final StudyStreakRepository studyStreakRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GrammarTopicRepository grammarTopicRepository;
    private final UserSettingRepository userSettingRepository;

    @Value("${app.upload.dir:uploads/avatars}")
    private String uploadDir;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        int streak = studyStreakRepository.findByUserId(userId)
                .map(s -> s.getCurrentStreak() != null ? s.getCurrentStreak() : 0)
                .orElse(0);

        long totalVocab = vocabularyRepository.countByUserId(userId);
        long totalGrammar = grammarTopicRepository.countByUserId(userId);

        UserSetting setting = userSettingRepository.findByUserId(userId).orElse(null);
        int dailyTarget = setting != null && setting.getDailyLearningTarget() != null
                ? setting.getDailyLearningTarget()
                : 30;

        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .targetScore(user.getTargetScore() != null ? user.getTargetScore() : 650)
                .dailyLearningTarget(dailyTarget)
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .currentStreak(streak)
                .totalVocabulary(totalVocab)
                .totalGrammar(totalGrammar)
                .build();
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        if (StringUtils.hasText(request.getDisplayName())) {
            user.setDisplayName(request.getDisplayName().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getTargetScore() != null && request.getTargetScore() > 0) {
            user.setTargetScore(request.getTargetScore());
        }

        userRepository.save(user);

        // Update daily target in settings if provided
        if (request.getDailyLearningTarget() != null && request.getDailyLearningTarget() > 0) {
            UserSetting setting = userSettingRepository.findByUserId(userId).orElse(null);
            if (setting == null) {
                setting = UserSetting.builder()
                        .user(user)
                        .dailyLearningTarget(request.getDailyLearningTarget())
                        .build();
            } else {
                setting.setDailyLearningTarget(request.getDailyLearningTarget());
            }
            userSettingRepository.save(setting);
        }

        return getCurrentUser(userId);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User {} successfully changed password", user.getEmail());
    }

    @Transactional
    public String sendForgotPasswordOtp(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        return emailOtpService.generateAndSaveOtp(user.getEmail());
    }

    @Transactional(readOnly = true)
    public boolean verifyOtp(VerifyOtpRequest request) {
        return emailOtpService.verifyOtp(request.getEmail(), request.getOtpCode());
    }

    @Transactional
    public void resetPasswordWithOtp(ResetPasswordWithOtpRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        String email = request.getEmail().trim().toLowerCase();
        boolean isValid = emailOtpService.verifyOtp(email, request.getOtpCode());
        if (!isValid) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        emailOtpService.markOtpAsUsed(email, request.getOtpCode());
        log.info("User {} successfully reset password via email OTP", email);
    }

    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không được rỗng");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        try {
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = "avatar_" + userId + "_" + UUID.randomUUID() + extension;

            Path uploadPath = Paths.get("uploads/avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/api/auth/avatars/" + fileName;
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            return avatarUrl;
        } catch (IOException e) {
            log.error("Failed to store avatar file: {}", e.getMessage());
            throw new RuntimeException("Không thể lưu ảnh đại diện: " + e.getMessage());
        }
    }
}
