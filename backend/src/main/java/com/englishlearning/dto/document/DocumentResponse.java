package com.englishlearning.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String title;
    private String fileName;
    private String storedFileName;
    private String filePath;
    private String fileType;
    private String mimeType;
    private Long fileSize;
    private String formattedFileSize;
    private String category;
    private String description;
    private Integer downloadCount;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
