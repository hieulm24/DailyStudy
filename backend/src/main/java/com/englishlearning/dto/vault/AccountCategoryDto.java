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
public class AccountCategoryDto {
    private Long id;
    private String code;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Boolean isSystem;
    private Long credentialCount;
    private LocalDateTime createdAt;
}
