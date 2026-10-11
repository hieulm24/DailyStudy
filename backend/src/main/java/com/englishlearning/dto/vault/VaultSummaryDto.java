package com.englishlearning.dto.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaultSummaryDto {
    private long totalAccounts;
    private long favoriteCount;
    private long weakPasswordCount;
    private long mediumPasswordCount;
    private long strongPasswordCount;
    private long trashCount;
    private long totalAttachments;
    private List<CategoryCountDto> categoryBreakdowns;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryCountDto {
        private Long categoryId;
        private String categoryCode;
        private String categoryName;
        private String icon;
        private String color;
        private long count;
    }
}
