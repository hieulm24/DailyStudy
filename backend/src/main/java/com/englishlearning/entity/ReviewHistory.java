package com.englishlearning.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_item_id", nullable = false)
    @JsonIgnore
    private ReviewItem reviewItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "result", nullable = false, length = 30)
    private String result; // FORGOT, HARD, GOOD, EASY

    @Column(name = "difficulty", length = 30)
    private String difficulty;

    @Column(name = "previous_mastery_level")
    private Integer previousMasteryLevel;

    @Column(name = "new_mastery_level")
    private Integer newMasteryLevel;

    @Column(name = "previous_interval_days")
    private Integer previousIntervalDays;

    @Column(name = "new_interval_days")
    private Integer newIntervalDays;

    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        if (this.reviewedAt == null) this.reviewedAt = LocalDateTime.now();
    }
}
