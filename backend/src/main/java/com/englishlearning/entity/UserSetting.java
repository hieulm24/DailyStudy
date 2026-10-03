package com.englishlearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "theme", nullable = false, length = 20)
    @Builder.Default
    private String theme = "LIGHT";

    @Column(name = "language", nullable = false, length = 10)
    @Builder.Default
    private String language = "vi";

    @Column(name = "timezone", nullable = false, length = 100)
    @Builder.Default
    private String timezone = "Asia/Ho_Chi_Minh";

    @Column(name = "daily_learning_target", nullable = false)
    @Builder.Default
    private Integer dailyLearningTarget = 30;

    @Column(name = "review_enabled", nullable = false)
    @Builder.Default
    private Boolean reviewEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null)
            this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null)
            this.updatedAt = LocalDateTime.now();
        if (this.theme == null)
            this.theme = "LIGHT";
        if (this.language == null)
            this.language = "vi";
        if (this.timezone == null)
            this.timezone = "Asia/Ho_Chi_Minh";
        if (this.dailyLearningTarget == null)
            this.dailyLearningTarget = 30;
        if (this.reviewEnabled == null)
            this.reviewEnabled = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
