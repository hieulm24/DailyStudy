package com.englishlearning.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSubmitRequest {
    @NotNull(message = "Review Item ID không được để trống")
    private Long reviewItemId;

    @NotBlank(message = "Kết quả đánh giá không được để trống")
    private String result; // FORGOT, HARD, GOOD, EASY

    private String difficulty;
}
