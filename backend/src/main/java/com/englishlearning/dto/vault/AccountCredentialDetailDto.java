package com.englishlearning.dto.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCredentialDetailDto {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryCode;
    private String categoryColor;
    private String categoryIcon;

    private String title;
    private String serviceName;
    private String websiteUrl;
    private String appPackageName;

    private String username;
    private String email;
    private String phoneNumber;
    private String password; // Decrypted password
    private String recoveryEmail;
    private Long linkedCredentialId;
    private String linkedCredentialTitle;

    private String pinCode;
    private String securityQuestions;
    private String totpSecret;
    private String recoveryCodes;
    private String customFields; // JSON string
    private String notes;

    private String passwordStrength;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime lastAccessedAt;

    private Boolean isFavorite;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    @Builder.Default
    private List<AccountAttachmentDto> attachments = new ArrayList<>();

    @Builder.Default
    private List<AccountPasswordHistoryDto> passwordHistories = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
