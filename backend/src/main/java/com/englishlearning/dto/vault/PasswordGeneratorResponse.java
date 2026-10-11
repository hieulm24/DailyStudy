package com.englishlearning.dto.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordGeneratorResponse {
    private String password;
    private String strength; // WEAK, MEDIUM, STRONG, VERY_STRONG
    private int score; // 0 - 100
}
