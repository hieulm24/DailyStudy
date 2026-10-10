package com.englishlearning.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Tên hiển thị không được để trống")
    private String displayName;

    private String avatarUrl;
    private String phoneNumber;
    private String bio;
    private Integer targetScore;
    private Integer dailyLearningTarget;
}
