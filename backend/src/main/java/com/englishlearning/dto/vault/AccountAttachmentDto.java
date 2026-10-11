package com.englishlearning.dto.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountAttachmentDto {
    private Long id;
    private Long credentialId;
    private String fileName;
    private String storedFileName;
    private String filePath;
    private String fileType;
    private String mimeType;
    private Long fileSize;
    private String description;
    private Integer downloadCount;
    private LocalDateTime createdAt;
}
