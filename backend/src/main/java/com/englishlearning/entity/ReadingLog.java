package com.englishlearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
    @Table(name = "reading_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "pages_read", nullable = false)
    @Builder.Default
    private Integer pagesRead = 0;

    @Column(name = "minutes_read")
    private Integer minutesRead;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.logDate == null) {
            this.logDate = LocalDate.now();
        }
        if (this.pagesRead == null) {
            this.pagesRead = 0;
        }
    }
}
