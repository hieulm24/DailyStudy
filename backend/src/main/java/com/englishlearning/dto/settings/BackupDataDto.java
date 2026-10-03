package com.englishlearning.dto.settings;

import com.englishlearning.dto.grammar.GrammarResponse;
import com.englishlearning.dto.listening.ListeningResponse;
import com.englishlearning.dto.speaking.SpeakingResponse;
import com.englishlearning.dto.vocabulary.VocabularyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupDataDto {
    private String version;
    private LocalDateTime exportDate;
    private UserSettingDto settings;
    private List<VocabularyResponse> vocabularies;
    private List<GrammarResponse> grammars;
    private List<ListeningResponse> listenings;
    private List<SpeakingResponse> speakings;
}
