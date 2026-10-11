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
public class AccountPasswordHistoryDto {
    private Long id;
    private Long credentialId;
    private String oldPassword; // Decrypted when returned to user
    private String changedReason;
    private LocalDateTime changedAt;
}
