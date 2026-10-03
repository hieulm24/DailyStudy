package com.englishlearning.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryDefinitionDTO {
    private String definition;
    private String example;
    private List<String> synonyms;
    private List<String> antonyms;
}
