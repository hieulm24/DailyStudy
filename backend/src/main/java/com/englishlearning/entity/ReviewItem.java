package com.englishlearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content_type", nullable = false, length = 30)
    private String contentType; // VOCABULARY, GRAMMAR

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "review_status", nullable = false, length = 30)
    @Builder.Default
    private String reviewStatus = "ACTIVE"; // ACTIVE, PAUSED, MASTERED

    @Column(name = "mastery_level", nullable = false)
    @Builder.Default
    private Integer masteryLevel = 0;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "current_interval_days", nullable = false)
    @Builder.Default
    private Integer currentIntervalDays = 1;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "reviewItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReviewHistory> histories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.reviewStatus == null) this.reviewStatus = "ACTIVE";
        if (this.masteryLevel == null) this.masteryLevel = 0;
        if (this.reviewCount == null) this.reviewCount = 0;
        if (this.currentIntervalDays == null) this.currentIntervalDays = 1;
        if (this.nextReviewAt == null) this.nextReviewAt = LocalDateTime.now().plusDays(1);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
