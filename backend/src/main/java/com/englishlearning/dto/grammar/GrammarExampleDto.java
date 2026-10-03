package com.englishlearning.dto.grammar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarExampleDto {
    private Long id;
    private String exampleSentence;
    private String meaning;
    private Boolean isPrimary;
}
