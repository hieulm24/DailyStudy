package com.englishlearning.dto.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordGeneratorRequest {
    @Builder.Default
    private int length = 16;
    @Builder.Default
    private boolean includeUppercase = true;
    @Builder.Default
    private boolean includeLowercase = true;
    @Builder.Default
    private boolean includeNumbers = true;
    @Builder.Default
    private boolean includeSymbols = true;
    @Builder.Default
    private boolean excludeAmbiguous = false;
}
