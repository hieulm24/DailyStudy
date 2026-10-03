package com.englishlearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grammar_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @Column(name = "level", length = 20)
    private String level;

    @Column(name = "structure", length = 2000)
    private String structure;

    @Column(name = "positive_structure", length = 2000)
    private String positiveStructure;

    @Column(name = "negative_structure", length = 2000)
    private String negativeStructure;

    @Column(name = "question_structure", length = 2000)
    private String questionStructure;

    @Column(name = "usage", columnDefinition = "NVARCHAR(MAX)")
    private String usage;

    @Column(name = "signal_words", columnDefinition = "NVARCHAR(MAX)")
    private String signalWords;

    @Column(name = "common_mistakes", columnDefinition = "NVARCHAR(MAX)")
    private String commonMistakes;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "NEW"; // NEW, LEARNING, REVIEW, MASTERED

    @Column(name = "mastery_level", nullable = false)
    @Builder.Default
    private Integer masteryLevel = 0;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "grammarTopic", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<GrammarExample> examples = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "NEW";
        if (this.masteryLevel == null) this.masteryLevel = 0;
        if (this.reviewCount == null) this.reviewCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
