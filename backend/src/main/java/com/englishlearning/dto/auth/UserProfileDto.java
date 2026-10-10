package com.englishlearning.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String phoneNumber;
    private String bio;
    private Integer targetScore;
    private Integer dailyLearningTarget;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    
    // Summary statistics for profile card
    private Integer currentStreak;
    private Long totalVocabulary;
    private Long totalGrammar;
    private Long totalActivities;
}
