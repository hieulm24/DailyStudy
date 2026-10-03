package com.englishlearning.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingDto {
    private Long id;
    private String theme; // LIGHT, DARK
    private String language; // vi, en
    private String timezone;
    private Integer dailyLearningTarget;
    private Boolean reviewEnabled;
    private String displayName;
    private String email;
}
