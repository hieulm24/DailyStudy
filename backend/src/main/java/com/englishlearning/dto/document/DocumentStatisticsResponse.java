package com.englishlearning.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatisticsResponse {
    private Long totalDocuments;
    private Long totalFileSizeBytes;
    private String formattedTotalSize;
    private Long totalExcel;
    private Long totalWord;
    private Long totalPdf;
    private Long totalPowerPoint;
    private Long totalOther;
    private Long totalFavorites;
    private Map<String, Long> countByCategory;
}
