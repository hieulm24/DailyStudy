package com.englishlearning.dto.it;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItChatSessionDto {
    private Long id;
    private String title;
    private String topicCategory;
    private List<ItChatMessageDto> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
