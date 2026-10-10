package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.it.CreateOrUpdateSnippetRequest;
import com.englishlearning.dto.it.ItCodeSnippetDto;
import com.englishlearning.entity.ItCodeSnippet;
import com.englishlearning.entity.User;
import com.englishlearning.repository.ItCodeSnippetRepository;
import com.englishlearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItSnippetService {

    private final ItCodeSnippetRepository snippetRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<ItCodeSnippetDto> getSnippets(Long userId, String language, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ItCodeSnippet> snippetPage = snippetRepository.searchSnippets(userId, language, search, pageable);

        List<ItCodeSnippetDto> dtos = snippetPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponse.<ItCodeSnippetDto>builder()
                .items(dtos)
                .page(snippetPage.getNumber())
                .size(snippetPage.getSize())
                .totalElements(snippetPage.getTotalElements())
                .totalPages(snippetPage.getTotalPages())
                .isFirst(snippetPage.isFirst())
                .isLast(snippetPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public ItCodeSnippetDto getSnippetById(Long userId, Long snippetId) {
        ItCodeSnippet snippet = snippetRepository.findByIdAndUserId(snippetId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đoạn code với ID: " + snippetId));
        return mapToDto(snippet);
    }

    @Transactional
    public ItCodeSnippetDto createSnippet(Long userId, CreateOrUpdateSnippetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        ItCodeSnippet snippet = ItCodeSnippet.builder()
                .user(user)
                .title(request.getTitle().trim())
                .language(StringUtils.hasText(request.getLanguage()) ? request.getLanguage().toUpperCase() : "JAVA")
                .codeContent(request.getCodeContent())
                .explanation(request.getExplanation())
                .tags(request.getTags())
                .build();

        snippet = snippetRepository.save(snippet);
        return mapToDto(snippet);
    }

    @Transactional
    public ItCodeSnippetDto updateSnippet(Long userId, Long snippetId, CreateOrUpdateSnippetRequest request) {
        ItCodeSnippet snippet = snippetRepository.findByIdAndUserId(snippetId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đoạn code với ID: " + snippetId));

        snippet.setTitle(request.getTitle().trim());
        if (StringUtils.hasText(request.getLanguage())) {
            snippet.setLanguage(request.getLanguage().toUpperCase());
        }
        snippet.setCodeContent(request.getCodeContent());
        snippet.setExplanation(request.getExplanation());
        snippet.setTags(request.getTags());

        snippet = snippetRepository.save(snippet);
        return mapToDto(snippet);
    }

    @Transactional
    public void deleteSnippet(Long userId, Long snippetId) {
        ItCodeSnippet snippet = snippetRepository.findByIdAndUserId(snippetId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đoạn code với ID: " + snippetId));
        snippetRepository.delete(snippet);
    }

    private ItCodeSnippetDto mapToDto(ItCodeSnippet s) {
        return ItCodeSnippetDto.builder()
                .id(s.getId())
                .title(s.getTitle())
                .language(s.getLanguage())
                .codeContent(s.getCodeContent())
                .explanation(s.getExplanation())
                .tags(s.getTags())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
