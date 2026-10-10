package com.englishlearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vocabularies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private VocabularyTopic topic;

    @Column(name = "word", nullable = false, length = 255)
    private String word;

    @Column(name = "meaning", nullable = false, length = 1000)
    private String meaning;

    @Column(name = "pronunciation", length = 255)
    private String pronunciation;

    @Column(name = "part_of_speech", length = 100)
    private String partOfSpeech;

    @Column(name = "level", length = 20)
    private String level;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Column(name = "context_sentence", columnDefinition = "NVARCHAR(MAX)")
    private String contextSentence;

    @Column(name = "context_meaning", columnDefinition = "NVARCHAR(MAX)")
    private String contextMeaning;

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

    @OneToMany(mappedBy = "vocabulary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VocabularyExample> examples = new ArrayList<>();

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
