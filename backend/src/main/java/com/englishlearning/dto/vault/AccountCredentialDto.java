package com.englishlearning.dto.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCredentialDto {
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
    private String password; // Decrypted password for 1-click copy
    private String recoveryEmail;
    private Long linkedCredentialId;
    private String linkedCredentialTitle;

    private String pinCode;
    private String passwordStrength; // WEAK, MEDIUM, STRONG, VERY_STRONG
    private LocalDateTime passwordChangedAt;
    private LocalDateTime lastAccessedAt;

    private Boolean isFavorite;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    private Integer attachmentCount;
    private Integer passwordHistoryCount;
    private Boolean hasTotp;
    private Boolean hasNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
