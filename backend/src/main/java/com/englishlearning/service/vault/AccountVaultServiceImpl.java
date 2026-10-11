package com.englishlearning.service.vault;

import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.vault.*;
import com.englishlearning.entity.User;
import com.englishlearning.entity.vault.AccountCategory;
import com.englishlearning.entity.vault.AccountCredential;
import com.englishlearning.entity.vault.AccountCredentialAttachment;
import com.englishlearning.entity.vault.AccountPasswordHistory;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.repository.vault.AccountCategoryRepository;
import com.englishlearning.repository.vault.AccountCredentialAttachmentRepository;
import com.englishlearning.repository.vault.AccountCredentialRepository;
import com.englishlearning.repository.vault.AccountPasswordHistoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountVaultServiceImpl implements AccountVaultService {

    private final AccountCategoryRepository categoryRepository;
    private final AccountCredentialRepository credentialRepository;
    private final AccountPasswordHistoryRepository historyRepository;
    private final AccountCredentialAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final VaultEncryptionService encryptionService;

    private final Path fileStorageLocation = Paths.get("uploads/vault-attachments").toAbsolutePath().normalize();
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create vault uploads directory", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountCategoryDto> getCategories(Long userId) {
        List<AccountCategory> categories = categoryRepository.findAllByUserIdOrSystem(userId);
        return categories.stream().map(cat -> AccountCategoryDto.builder()
                .id(cat.getId())
                .code(cat.getCode())
                .name(cat.getName())
                .icon(cat.getIcon())
                .color(cat.getColor())
                .sortOrder(cat.getSortOrder())
                .isSystem(cat.getIsSystem())
                .createdAt(cat.getCreatedAt())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccountCredentialDto> getCredentials(
            Long userId,
            Long categoryId,
            Boolean isFavorite,
            Boolean isDeleted,
            String search,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Boolean deletedFilter = (isDeleted != null) ? isDeleted : false;

        Page<AccountCredential> pageData = credentialRepository.findWithFilters(
                userId,
                deletedFilter,
                categoryId,
                isFavorite,
                search,
                pageable
        );

        List<AccountCredentialDto> items = pageData.getContent().stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        return PageResponse.from(pageData, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountCredentialDto> getAllActiveCredentials(Long userId) {
        return credentialRepository.findByUserIdAndIsDeletedFalseOrderByTitleAsc(userId).stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountCredentialDetailDto getCredentialDetail(Long userId, Long id) {
        AccountCredential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        credential.setLastAccessedAt(LocalDateTime.now());
        credentialRepository.save(credential);

        return mapToDetailDto(credential);
    }

    @Override
    @Transactional
    public AccountCredentialDetailDto createCredential(Long userId, CreateAccountCredentialRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        AccountCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        String strength = evaluatePasswordStrength(request.getPassword());
        String encryptedPassword = encryptionService.encrypt(request.getPassword());

        AccountCredential credential = AccountCredential.builder()
                .user(user)
                .category(category)
                .title(request.getTitle().trim())
                .serviceName(StringUtils.hasText(request.getServiceName()) ? request.getServiceName().trim() : null)
                .websiteUrl(StringUtils.hasText(request.getWebsiteUrl()) ? request.getWebsiteUrl().trim() : null)
                .appPackageName(request.getAppPackageName())
                .username(StringUtils.hasText(request.getUsername()) ? request.getUsername().trim() : null)
                .email(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null)
                .phoneNumber(StringUtils.hasText(request.getPhoneNumber()) ? request.getPhoneNumber().trim() : null)
                .passwordEncrypted(encryptedPassword)
                .recoveryEmail(request.getRecoveryEmail())
                .linkedCredentialId(request.getLinkedCredentialId())
                .pinCode(request.getPinCode())
                .securityQuestions(request.getSecurityQuestions())
                .totpSecret(request.getTotpSecret())
                .recoveryCodes(request.getRecoveryCodes())
                .customFields(request.getCustomFields())
                .notes(request.getNotes())
                .passwordStrength(strength)
                .passwordChangedAt(LocalDateTime.now())
                .isFavorite(Boolean.TRUE.equals(request.getIsFavorite()))
                .isDeleted(false)
                .build();

        AccountCredential saved = credentialRepository.save(credential);
        return mapToDetailDto(saved);
    }

    @Override
    @Transactional
    public AccountCredentialDetailDto updateCredential(Long userId, Long id, UpdateAccountCredentialRequest request) {
        AccountCredential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        AccountCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }
        credential.setCategory(category);

        credential.setTitle(request.getTitle().trim());
        credential.setServiceName(StringUtils.hasText(request.getServiceName()) ? request.getServiceName().trim() : null);
        credential.setWebsiteUrl(StringUtils.hasText(request.getWebsiteUrl()) ? request.getWebsiteUrl().trim() : null);
        credential.setAppPackageName(request.getAppPackageName());
        credential.setUsername(StringUtils.hasText(request.getUsername()) ? request.getUsername().trim() : null);
        credential.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        credential.setPhoneNumber(StringUtils.hasText(request.getPhoneNumber()) ? request.getPhoneNumber().trim() : null);
        credential.setRecoveryEmail(request.getRecoveryEmail());
        credential.setLinkedCredentialId(request.getLinkedCredentialId());
        credential.setPinCode(request.getPinCode());
        credential.setSecurityQuestions(request.getSecurityQuestions());
        credential.setTotpSecret(request.getTotpSecret());
        credential.setRecoveryCodes(request.getRecoveryCodes());
        credential.setCustomFields(request.getCustomFields());
        credential.setNotes(request.getNotes());
        if (request.getIsFavorite() != null) {
            credential.setIsFavorite(request.getIsFavorite());
        }

        // Check if password was changed
        String oldPassword = encryptionService.decrypt(credential.getPasswordEncrypted());
        if (!oldPassword.equals(request.getPassword())) {
            // Save to password history
            AccountPasswordHistory history = AccountPasswordHistory.builder()
                    .credential(credential)
                    .oldPasswordEncrypted(credential.getPasswordEncrypted())
                    .changedReason(StringUtils.hasText(request.getPasswordChangeReason()) ? request.getPasswordChangeReason() : "Cập nhật mật khẩu mới")
                    .changedAt(LocalDateTime.now())
                    .build();
            historyRepository.save(history);

            // Update password & recalculate strength
            credential.setPasswordEncrypted(encryptionService.encrypt(request.getPassword()));
            credential.setPasswordStrength(evaluatePasswordStrength(request.getPassword()));
            credential.setPasswordChangedAt(LocalDateTime.now());
        }

        AccountCredential updated = credentialRepository.save(credential);
        return mapToDetailDto(updated);
    }

    @Override
    @Transactional
    public void toggleFavorite(Long userId, Long id) {
        AccountCredential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        credential.setIsFavorite(!Boolean.TRUE.equals(credential.getIsFavorite()));
        credentialRepository.save(credential);
    }

    @Override
    @Transactional
    public void moveToTrash(Long userId, Long id) {
        AccountCredential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        credential.setIsDeleted(true);
        credential.setDeletedAt(LocalDateTime.now());
        credentialRepository.save(credential);
    }

    @Override
    @Transactional
    public void restoreFromTrash(Long userId, Long id) {
        AccountCredential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        credential.setIsDeleted(false);
        credential.setDeletedAt(null);
        credentialRepository.save(credential);
    }

    @Override
    @Transactional
    public void deletePermanently(Long userId, Long id) {
        AccountCredential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        // Delete physical files
        for (AccountCredentialAttachment att : credential.getAttachments()) {
            try {
                Path filePath = Paths.get(att.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                log.warn("Failed to delete physical file {}: {}", att.getFilePath(), e.getMessage());
            }
        }

        credentialRepository.delete(credential);
    }

    @Override
    @Transactional(readOnly = true)
    public VaultSummaryDto getSummary(Long userId) {
        long total = credentialRepository.countByUserIdAndIsDeletedFalse(userId);
        long favorite = credentialRepository.countByUserIdAndIsFavoriteTrueAndIsDeletedFalse(userId);
        long weak = credentialRepository.countByUserIdAndPasswordStrengthAndIsDeletedFalse(userId, "WEAK");
        long medium = credentialRepository.countByUserIdAndPasswordStrengthAndIsDeletedFalse(userId, "MEDIUM");
        long strong = credentialRepository.countByUserIdAndPasswordStrengthAndIsDeletedFalse(userId, "STRONG")
                     + credentialRepository.countByUserIdAndPasswordStrengthAndIsDeletedFalse(userId, "VERY_STRONG");
        long trash = credentialRepository.countByUserIdAndIsDeletedTrue(userId);
        long attachments = attachmentRepository.countByUserId(userId);

        List<AccountCategory> categories = categoryRepository.findAllByUserIdOrSystem(userId);
        List<VaultSummaryDto.CategoryCountDto> breakdowns = new ArrayList<>();
        for (AccountCategory cat : categories) {
            Page<AccountCredential> page = credentialRepository.findWithFilters(userId, false, cat.getId(), null, null, PageRequest.of(0, 1));
            breakdowns.add(VaultSummaryDto.CategoryCountDto.builder()
                    .categoryId(cat.getId())
                    .categoryCode(cat.getCode())
                    .categoryName(cat.getName())
                    .icon(cat.getIcon())
                    .color(cat.getColor())
                    .count(page.getTotalElements())
                    .build());
        }

        return VaultSummaryDto.builder()
                .totalAccounts(total)
                .favoriteCount(favorite)
                .weakPasswordCount(weak)
                .mediumPasswordCount(medium)
                .strongPasswordCount(strong)
                .trashCount(trash)
                .totalAttachments(attachments)
                .categoryBreakdowns(breakdowns)
                .build();
    }

    @Override
    public PasswordGeneratorResponse generatePassword(PasswordGeneratorRequest request) {
        String chars = "";
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ"; // without ambiguous letters if requested
        String lower = "abcdefghijkmnopqrstuvwxyz";
        String digits = "23456789";
        String symbols = "!@#$%^&*()-_=+[]{}|;:,.<>?";

        if (!request.isExcludeAmbiguous()) {
            upper += "IO";
            lower += "l";
            digits += "01";
        }

        StringBuilder pool = new StringBuilder();
        if (request.isIncludeUppercase()) pool.append(upper);
        if (request.isIncludeLowercase()) pool.append(lower);
        if (request.isIncludeNumbers()) pool.append(digits);
        if (request.isIncludeSymbols()) pool.append(symbols);

        if (pool.length() == 0) {
            pool.append(lower).append(digits);
        }

        int length = Math.max(6, Math.min(request.getLength(), 128));
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(pool.length());
            password.append(pool.charAt(index));
        }

        String pass = password.toString();
        String strength = evaluatePasswordStrength(pass);
        int score = calculateStrengthScore(pass);

        return PasswordGeneratorResponse.builder()
                .password(pass)
                .strength(strength)
                .score(score)
                .build();
    }

    @Override
    @Transactional
    public AccountAttachmentDto uploadAttachment(Long userId, Long credentialId, MultipartFile file, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        AccountCredential credential = credentialRepository.findByIdAndUserId(credentialId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Tệp tin không được để trống");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";
        int extIndex = originalFilename.lastIndexOf('.');
        if (extIndex > 0) {
            extension = originalFilename.substring(extIndex + 1).toLowerCase();
        }

        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path targetLocation = this.fileStorageLocation.resolve(storedFilename);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu trữ tệp tin: " + originalFilename, ex);
        }

        AccountCredentialAttachment attachment = AccountCredentialAttachment.builder()
                .credential(credential)
                .user(user)
                .fileName(originalFilename)
                .storedFileName(storedFilename)
                .filePath(targetLocation.toString())
                .fileType(extension)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .description(description)
                .downloadCount(0)
                .build();

        AccountCredentialAttachment saved = attachmentRepository.save(attachment);
        return mapAttachmentToDto(saved);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long userId, Long attachmentId) {
        AccountCredentialAttachment attachment = attachmentRepository.findByIdAndUserId(attachmentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tệp đính kèm"));

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            log.warn("Failed to delete physical file {}: {}", attachment.getFilePath(), e.getMessage());
        }

        attachmentRepository.delete(attachment);
    }

    @Override
    @Transactional
    public Resource loadAttachmentResource(Long userId, Long attachmentId) {
        AccountCredentialAttachment attachment = attachmentRepository.findByIdAndUserId(attachmentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tệp đính kèm"));

        try {
            Path filePath = Paths.get(attachment.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                attachment.setDownloadCount(attachment.getDownloadCount() + 1);
                attachmentRepository.save(attachment);
                return resource;
            } else {
                throw new RuntimeException("Tệp tin không tồn tại trên hệ thống");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Đường dẫn tệp tin không hợp lệ", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AccountAttachmentDto getAttachmentMetadata(Long userId, Long attachmentId) {
        AccountCredentialAttachment attachment = attachmentRepository.findByIdAndUserId(attachmentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tệp đính kèm"));
        return mapAttachmentToDto(attachment);
    }

    // --- Helpers ---

    private AccountCredentialDto mapToSummaryDto(AccountCredential c) {
        String decryptedPassword = encryptionService.decrypt(c.getPasswordEncrypted());
        int attachmentsCount = c.getAttachments() != null ? c.getAttachments().size() : 0;
        int historiesCount = c.getPasswordHistories() != null ? c.getPasswordHistories().size() : 0;

        String linkedTitle = null;
        if (c.getLinkedCredentialId() != null) {
            linkedTitle = credentialRepository.findById(c.getLinkedCredentialId()).map(AccountCredential::getTitle).orElse(null);
        }

        return AccountCredentialDto.builder()
                .id(c.getId())
                .categoryId(c.getCategory() != null ? c.getCategory().getId() : null)
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : "Chưa phân loại")
                .categoryCode(c.getCategory() != null ? c.getCategory().getCode() : "OTHER")
                .categoryColor(c.getCategory() != null ? c.getCategory().getColor() : "#64748B")
                .categoryIcon(c.getCategory() != null ? c.getCategory().getIcon() : "Key")
                .title(c.getTitle())
                .serviceName(c.getServiceName())
                .websiteUrl(c.getWebsiteUrl())
                .appPackageName(c.getAppPackageName())
                .username(c.getUsername())
                .email(c.getEmail())
                .phoneNumber(c.getPhoneNumber())
                .password(decryptedPassword)
                .recoveryEmail(c.getRecoveryEmail())
                .linkedCredentialId(c.getLinkedCredentialId())
                .linkedCredentialTitle(linkedTitle)
                .pinCode(c.getPinCode())
                .passwordStrength(c.getPasswordStrength())
                .passwordChangedAt(c.getPasswordChangedAt())
                .lastAccessedAt(c.getLastAccessedAt())
                .isFavorite(c.getIsFavorite())
                .isDeleted(c.getIsDeleted())
                .deletedAt(c.getDeletedAt())
                .attachmentCount(attachmentsCount)
                .passwordHistoryCount(historiesCount)
                .hasTotp(StringUtils.hasText(c.getTotpSecret()))
                .hasNotes(StringUtils.hasText(c.getNotes()))
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private AccountCredentialDetailDto mapToDetailDto(AccountCredential c) {
        String decryptedPassword = encryptionService.decrypt(c.getPasswordEncrypted());

        String linkedTitle = null;
        if (c.getLinkedCredentialId() != null) {
            linkedTitle = credentialRepository.findById(c.getLinkedCredentialId()).map(AccountCredential::getTitle).orElse(null);
        }

        List<AccountAttachmentDto> attachments = attachmentRepository.findByCredentialIdOrderByCreatedAtDesc(c.getId())
                .stream().map(this::mapAttachmentToDto).collect(Collectors.toList());

        List<AccountPasswordHistoryDto> histories = historyRepository.findByCredentialIdOrderByChangedAtDesc(c.getId())
                .stream().map(h -> AccountPasswordHistoryDto.builder()
                        .id(h.getId())
                        .credentialId(c.getId())
                        .oldPassword(encryptionService.decrypt(h.getOldPasswordEncrypted()))
                        .changedReason(h.getChangedReason())
                        .changedAt(h.getChangedAt())
                        .build()).collect(Collectors.toList());

        return AccountCredentialDetailDto.builder()
                .id(c.getId())
                .categoryId(c.getCategory() != null ? c.getCategory().getId() : null)
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : "Chưa phân loại")
                .categoryCode(c.getCategory() != null ? c.getCategory().getCode() : "OTHER")
                .categoryColor(c.getCategory() != null ? c.getCategory().getColor() : "#64748B")
                .categoryIcon(c.getCategory() != null ? c.getCategory().getIcon() : "Key")
                .title(c.getTitle())
                .serviceName(c.getServiceName())
                .websiteUrl(c.getWebsiteUrl())
                .appPackageName(c.getAppPackageName())
                .username(c.getUsername())
                .email(c.getEmail())
                .phoneNumber(c.getPhoneNumber())
                .password(decryptedPassword)
                .recoveryEmail(c.getRecoveryEmail())
                .linkedCredentialId(c.getLinkedCredentialId())
                .linkedCredentialTitle(linkedTitle)
                .pinCode(c.getPinCode())
                .securityQuestions(c.getSecurityQuestions())
                .totpSecret(c.getTotpSecret())
                .recoveryCodes(c.getRecoveryCodes())
                .customFields(c.getCustomFields())
                .notes(c.getNotes())
                .passwordStrength(c.getPasswordStrength())
                .passwordChangedAt(c.getPasswordChangedAt())
                .lastAccessedAt(c.getLastAccessedAt())
                .isFavorite(c.getIsFavorite())
                .isDeleted(c.getIsDeleted())
                .deletedAt(c.getDeletedAt())
                .attachments(attachments)
                .passwordHistories(histories)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private AccountAttachmentDto mapAttachmentToDto(AccountCredentialAttachment a) {
        return AccountAttachmentDto.builder()
                .id(a.getId())
                .credentialId(a.getCredential() != null ? a.getCredential().getId() : null)
                .fileName(a.getFileName())
                .storedFileName(a.getStoredFileName())
                .filePath(a.getFilePath())
                .fileType(a.getFileType())
                .mimeType(a.getMimeType())
                .fileSize(a.getFileSize())
                .description(a.getDescription())
                .downloadCount(a.getDownloadCount())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private String evaluatePasswordStrength(String password) {
        int score = calculateStrengthScore(password);
        if (score >= 80) return "VERY_STRONG";
        if (score >= 60) return "STRONG";
        if (score >= 40) return "MEDIUM";
        return "WEAK";
    }

    private int calculateStrengthScore(String password) {
        if (!StringUtils.hasText(password)) return 0;
        int score = 0;
        int length = password.length();

        if (length >= 8) score += 20;
        if (length >= 12) score += 20;
        if (length >= 16) score += 10;

        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        if (hasLower) score += 10;
        if (hasUpper) score += 10;
        if (hasDigit) score += 15;
        if (hasSpecial) score += 15;

        return Math.min(100, score);
    }
}
