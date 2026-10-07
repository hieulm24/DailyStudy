package com.englishlearning.dto.vocabulary;

import lombok.Data;

@Data
public class VocabularyTopicFilterRequest {
    private String search;
    private String level;
    private String status;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
