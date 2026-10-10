package com.englishlearning.service;

import com.englishlearning.dto.it.*;
import com.englishlearning.entity.ItChatMessage;
import com.englishlearning.entity.ItChatSession;
import com.englishlearning.entity.User;
import com.englishlearning.repository.ItChatMessageRepository;
import com.englishlearning.repository.ItChatSessionRepository;
import com.englishlearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItChatService {

    private final ItChatSessionRepository sessionRepository;
    private final ItChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ItAiService itAiService;

    @Transactional(readOnly = true)
    public List<ItChatSessionDto> getSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::mapSessionToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItChatSessionDto getSessionById(Long userId, Long sessionId) {
        ItChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên trò chuyện"));
        return mapSessionToDto(session);
    }

    @Transactional
    public ItChatSessionDto sendMessage(Long userId, SendItChatRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        ItChatSession session;
        if (request.getSessionId() != null && request.getSessionId() > 0) {
            session = sessionRepository.findByIdAndUserId(request.getSessionId(), userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên chat"));
        } else {
            // Auto generate title
            String title = request.getPrompt().trim();
            if (title.length() > 60) {
                title = title.substring(0, 57) + "...";
            }
            session = ItChatSession.builder()
                    .user(user)
                    .title(title)
                    .topicCategory(StringUtils.hasText(request.getTopicCategory()) ? request.getTopicCategory() : "GENERAL_IT")
                    .build();
            session = sessionRepository.save(session);
        }

        // 1. Save USER message
        ItChatMessage userMsg = ItChatMessage.builder()
                .session(session)
                .senderRole("USER")
                .content(request.getPrompt().trim())
                .codeSnippet(request.getCodeSnippet())
                .language(request.getLanguage())
                .imageUrl(request.getImageUrl())
                .build();
        messageRepository.save(userMsg);

        // 2. Query AI Architect
        ItAiResponseDto aiRes = itAiService.askArchitect(
                request.getPrompt(),
                request.getCodeSnippet(),
                request.getLanguage(),
                session.getTopicCategory(),
                request.getApiKey()
        );

        // 3. Save ASSISTANT message
        String fullContent = aiRes.getAnswerMarkdown();
        if (StringUtils.hasText(aiRes.getKeyTakeaways())) {
            fullContent += "\n\n---\n**💡 Key Takeaways (Tóm tắt cốt lõi):**\n" + aiRes.getKeyTakeaways();
        }

        ItChatMessage aiMsg = ItChatMessage.builder()
                .session(session)
                .senderRole("ASSISTANT")
                .content(fullContent)
                .codeSnippet(aiRes.getOptimizedCode())
                .language(aiRes.getLanguage())
                .mermaidDiagram(aiRes.getMermaidDiagram())
                .build();
        messageRepository.save(aiMsg);

        session.setUpdatedAt(LocalDateTime.now());
        session = sessionRepository.save(session);

        return mapSessionToDto(session);
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        ItChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên chat"));
        sessionRepository.delete(session);
    }

    private ItChatSessionDto mapSessionToDto(ItChatSession session) {
        List<ItChatMessage> rawMsgs = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        List<ItChatMessageDto> msgs = rawMsgs.stream()
                .map(m -> ItChatMessageDto.builder()
                        .id(m.getId())
                        .sessionId(session.getId())
                        .senderRole(m.getSenderRole())
                        .content(m.getContent())
                        .codeSnippet(m.getCodeSnippet())
                        .language(m.getLanguage())
                        .mermaidDiagram(m.getMermaidDiagram())
                        .imageUrl(m.getImageUrl())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ItChatSessionDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .topicCategory(session.getTopicCategory())
                .messages(msgs)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
