package com.englishlearning.dto.grammar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarExerciseQuestionDto {
    private Integer id; // 1, 2, 3...
    private String question; // Câu hỏi tiếng Anh chứa chỗ trống hoặc câu cần làm
    private String translation; // Dịch nghĩa của câu hỏi
    private List<String> options; // Danh sách 4 đáp án [A, B, C, D]
    private String correctAnswer; // Đáp án đúng (ví dụ "C" hoặc nội dung đáp án)
    private Integer correctIndex; // 0, 1, 2, 3
    private String explanation; // Lời giải thích ngữ pháp chi tiết của AI
    private String grammarTip; // Mẹo nhớ nhanh / Tip phòng thi TOEIC
    private String questionType; // MULTIPLE_CHOICE, FILL_IN_BLANK, ERROR_CORRECTION
    private String prompt; // Hướng dẫn (e.g. "Chia động từ trong ngoặc", "Tìm lỗi sai")
    private String baseWord; // Từ gốc trong ngoặc cho dạng điền từ (e.g. "submit", "complete")
    private String correctedWord; // Dạng sửa đúng cho câu lỗi sai
}
