package com.englishlearning.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponse {
    private String originalText;
    private String translatedText;
    private String phonetic;
    private String detectedLanguage;
    private DictionaryDataDTO dictionary;
    private String source;
    private boolean fromCache;
}
