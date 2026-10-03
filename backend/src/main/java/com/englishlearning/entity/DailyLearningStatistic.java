package com.englishlearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_learning_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLearningStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "statistic_date", nullable = false)
    private LocalDate statisticDate;

    @Column(name = "vocabulary_count", nullable = false)
    @Builder.Default
    private Integer vocabularyCount = 0;

    @Column(name = "grammar_count", nullable = false)
    @Builder.Default
    private Integer grammarCount = 0;

    @Column(name = "listening_count", nullable = false)
    @Builder.Default
    private Integer listeningCount = 0;

    @Column(name = "speaking_count", nullable = false)
    @Builder.Default
    private Integer speakingCount = 0;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "game_count", nullable = false)
    @Builder.Default
    private Integer gameCount = 0;

    @Column(name = "total_learning_count", nullable = false)
    @Builder.Default
    private Integer totalLearningCount = 0;

    @Column(name = "total_learning_seconds", nullable = false)
    @Builder.Default
    private Integer totalLearningSeconds = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.vocabularyCount == null) this.vocabularyCount = 0;
        if (this.grammarCount == null) this.grammarCount = 0;
        if (this.listeningCount == null) this.listeningCount = 0;
        if (this.speakingCount == null) this.speakingCount = 0;
        if (this.reviewCount == null) this.reviewCount = 0;
        if (this.gameCount == null) this.gameCount = 0;
        if (this.totalLearningCount == null) this.totalLearningCount = 0;
        if (this.totalLearningSeconds == null) this.totalLearningSeconds = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
