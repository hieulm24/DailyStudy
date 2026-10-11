package com.englishlearning.dto.vault;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountCredentialRequest {

    private Long categoryId;

    @NotBlank(message = "Tiêu đề tài khoản không được để trống")
    private String title;

    private String serviceName;
    private String websiteUrl;
    private String appPackageName;

    private String username;
    private String email;
    private String phoneNumber;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    private String recoveryEmail;
    private Long linkedCredentialId;
    private String pinCode;
    private String securityQuestions;
    private String totpSecret;
    private String recoveryCodes;
    private String customFields;
    private String notes;
    private Boolean isFavorite;
}
