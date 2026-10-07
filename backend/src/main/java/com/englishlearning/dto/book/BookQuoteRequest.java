package com.englishlearning.dto.book;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookQuoteRequest {

    @NotBlank(message = "Nội dung trích dẫn không được để trống")
    private String quoteText;

    private Integer pageNumber;

    private String chapter;

    private String note;

    private Boolean isFavorite;
}
