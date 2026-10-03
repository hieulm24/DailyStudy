package com.englishlearning.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    // Today
    private Integer todayVocabulary;
    private Integer todayGrammar;
    private Integer todayListening;
    private Integer todaySpeaking;
    private Integer todayTotal;
    private Long todayNeedReview;
    private Long todayCompletedReview;
    private Integer todayProgressPercent; // relative to daily target

    // Total counts
    private Long totalVocabulary;
    private Long totalGrammar;
    private Long totalListening;
    private Long totalSpeaking;
    private Long totalReviews;
    private Long totalGameSessions;

    // Streak
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastStudyDate;

    // Daily target
    private Integer dailyLearningTarget;

    // Recent activities
    private List<ActivityDto> recentActivities;
}
