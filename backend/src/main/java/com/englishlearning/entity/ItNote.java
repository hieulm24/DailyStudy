package com.englishlearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "it_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "category", nullable = false, length = 50)
    @Builder.Default
    private String category = "SYSTEM_DESIGN";

    @Column(name = "tags", length = 255)
    private String tags;

    @Column(name = "content_markdown", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String contentMarkdown;

    @Column(name = "diagram_mermaid", columnDefinition = "NVARCHAR(MAX)")
    private String diagramMermaid;

    @Column(name = "image_urls_json", columnDefinition = "NVARCHAR(MAX)")
    private String imageUrlsJson;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private Boolean isFavorite = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.isFavorite == null) this.isFavorite = false;
        if (this.category == null) this.category = "SYSTEM_DESIGN";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
