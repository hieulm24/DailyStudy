package com.englishlearning.dto.studylink;

import lombok.Data;

@Data
public class StudyLinkFilterRequest {
    private String search;
    private String category;
    private Boolean isFavorite;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
