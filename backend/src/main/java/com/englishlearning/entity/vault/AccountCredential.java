package com.englishlearning.entity.vault;

import com.englishlearning.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AccountCategory category;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "app_package_name", length = 150)
    private String appPackageName;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "password_encrypted", nullable = false, length = 1000)
    private String passwordEncrypted;

    @Column(name = "recovery_email", length = 255)
    private String recoveryEmail;

    @Column(name = "linked_credential_id")
    private Long linkedCredentialId;

    @Column(name = "pin_code", length = 50)
    private String pinCode;

    @Column(name = "security_questions", columnDefinition = "NVARCHAR(MAX)")
    private String securityQuestions;

    @Column(name = "totp_secret", length = 255)
    private String totpSecret;

    @Column(name = "recovery_codes", columnDefinition = "NVARCHAR(MAX)")
    private String recoveryCodes;

    @Column(name = "custom_fields", columnDefinition = "NVARCHAR(MAX)")
    private String customFields;

    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @Column(name = "password_strength", length = 20)
    @Builder.Default
    private String passwordStrength = "MEDIUM";

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private Boolean isFavorite = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "credential", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AccountCredentialAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "credential", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AccountPasswordHistory> passwordHistories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.isFavorite == null) {
            this.isFavorite = false;
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.passwordStrength == null) {
            this.passwordStrength = "MEDIUM";
        }
        if (this.passwordChangedAt == null) {
            this.passwordChangedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
