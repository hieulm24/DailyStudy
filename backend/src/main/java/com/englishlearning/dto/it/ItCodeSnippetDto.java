package com.englishlearning.dto.it;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItCodeSnippetDto {
    private Long id;
    private String title;
    private String language;
    private String codeContent;
    private String explanation;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
