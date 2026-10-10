package com.englishlearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grammar_exercise_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarExerciseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grammar_topic_id", nullable = false)
    private GrammarTopic grammarTopic;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "exercise_type", length = 50)
    @Builder.Default
    private String exerciseType = "MULTIPLE_CHOICE";

    @Column(name = "level", length = 20)
    private String level;

    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private Integer totalQuestions = 5;

    @Column(name = "correct_count", nullable = false)
    @Builder.Default
    private Integer correctCount = 0;

    @Column(name = "score", nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "questions_data", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String questionsData;

    @Column(name = "user_answers", columnDefinition = "NVARCHAR(MAX)")
    private String userAnswers;

    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Integer timeSpentSeconds = 0;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.completedAt == null) this.completedAt = LocalDateTime.now();
        if (this.exerciseType == null) this.exerciseType = "MULTIPLE_CHOICE";
        if (this.totalQuestions == null) this.totalQuestions = 5;
        if (this.correctCount == null) this.correctCount = 0;
        if (this.score == null) this.score = 0;
        if (this.timeSpentSeconds == null) this.timeSpentSeconds = 0;
    }
}
