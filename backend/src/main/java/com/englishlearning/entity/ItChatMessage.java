package com.englishlearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "it_chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ItChatSession session;

    @Column(name = "sender_role", nullable = false, length = 20)
    private String senderRole; // "USER" or "ASSISTANT"

    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "code_snippet", columnDefinition = "NVARCHAR(MAX)")
    private String codeSnippet;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "mermaid_diagram", columnDefinition = "NVARCHAR(MAX)")
    private String mermaidDiagram;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
