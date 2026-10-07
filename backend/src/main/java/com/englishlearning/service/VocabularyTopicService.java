package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.vocabulary.VocabularyRequest;
import com.englishlearning.dto.vocabulary.VocabularyTopicFilterRequest;
import com.englishlearning.dto.vocabulary.VocabularyTopicRequest;
import com.englishlearning.dto.vocabulary.VocabularyTopicResponse;
import com.englishlearning.entity.User;
import com.englishlearning.entity.VocabularyTopic;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.repository.VocabularyRepository;
import com.englishlearning.repository.VocabularyTopicRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VocabularyTopicService {

    private final VocabularyTopicRepository vocabularyTopicRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;
    private final VocabularyService vocabularyService;

    @Transactional(readOnly = true)
    public PageResponse<VocabularyTopicResponse> getTopics(Long userId, VocabularyTopicFilterRequest filter) {
        Specification<VocabularyTopic> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (StringUtils.hasText(filter.getSearch())) {
                String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), keyword),
                        cb.like(cb.lower(root.get("description")), keyword)
                ));
            }

            if (StringUtils.hasText(filter.getLevel())) {
                predicates.add(cb.equal(root.get("level"), filter.getLevel()));
            }

            if (StringUtils.hasText(filter.getStatus())) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                filter.getSortBy() != null ? filter.getSortBy() : "createdAt"
        );

        int pageNum = filter.getPage() != null && filter.getPage() >= 0 ? filter.getPage() : 0;
        int pageSize = filter.getSize() != null && filter.getSize() > 0 ? filter.getSize() : 20;

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        Page<VocabularyTopic> page = vocabularyTopicRepository.findAll(spec, pageable);

        List<VocabularyTopicResponse> dtoList = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(page, dtoList);
    }

    @Transactional(readOnly = true)
    public List<VocabularyTopicResponse> getAllTopics(Long userId) {
        return vocabularyTopicRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VocabularyTopicResponse getTopicById(Long id, Long userId) {
        VocabularyTopic topic = vocabularyTopicRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ đề từ vựng với id: " + id));
        return mapToResponse(topic);
    }

    @Transactional
    public VocabularyTopicResponse createTopic(Long userId, VocabularyTopicRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        VocabularyTopic topic = VocabularyTopic.builder()
                .user(user)
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .level(request.getLevel())
                .status(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "NEW")
                .build();

        VocabularyTopic savedTopic = vocabularyTopicRepository.save(topic);

        // Batch add vocabularies if provided in request
        if (request.getVocabularies() != null && !request.getVocabularies().isEmpty()) {
            for (VocabularyRequest vReq : request.getVocabularies()) {
                if (StringUtils.hasText(vReq.getWord()) && StringUtils.hasText(vReq.getMeaning())) {
                    vReq.setTopicId(savedTopic.getId());
                    if (!StringUtils.hasText(vReq.getLevel()) && StringUtils.hasText(savedTopic.getLevel())) {
                        vReq.setLevel(savedTopic.getLevel());
                    }
                    vocabularyService.createVocabulary(userId, vReq);
                }
            }
        }

        return mapToResponse(savedTopic);
    }

    @Transactional
    public VocabularyTopicResponse updateTopic(Long id, Long userId, VocabularyTopicRequest request) {
        VocabularyTopic topic = vocabularyTopicRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ đề từ vựng với id: " + id));

        topic.setName(request.getName().trim());
        topic.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        topic.setLevel(request.getLevel());
        if (StringUtils.hasText(request.getStatus())) {
            topic.setStatus(request.getStatus());
        }

        VocabularyTopic savedTopic = vocabularyTopicRepository.save(topic);
        return mapToResponse(savedTopic);
    }

    @Transactional
    public void deleteTopic(Long id, Long userId) {
        VocabularyTopic topic = vocabularyTopicRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ đề từ vựng với id: " + id));

        // Unlink or delete vocabularies
        var vocabs = vocabularyRepository.findByTopicId(id);
        for (var v : vocabs) {
            v.setTopic(null);
            vocabularyRepository.save(v);
        }

        vocabularyTopicRepository.delete(topic);
    }

    public VocabularyTopicResponse mapToResponse(VocabularyTopic topic) {
        long total = vocabularyTopicRepository.countVocabulariesByTopicId(topic.getId());
        long mastered = vocabularyTopicRepository.countVocabulariesByTopicIdAndStatus(topic.getId(), "MASTERED");
        long learning = vocabularyTopicRepository.countVocabulariesByTopicIdAndStatus(topic.getId(), "LEARNING");
        long newCount = vocabularyTopicRepository.countVocabulariesByTopicIdAndStatus(topic.getId(), "NEW");

        // Auto-update topic status based on progress if it has words
        String currentStatus = topic.getStatus();
        if (total > 0 && mastered == total && !"MASTERED".equals(currentStatus)) {
            currentStatus = "MASTERED";
        } else if (total > 0 && (learning > 0 || mastered > 0) && "NEW".equals(currentStatus)) {
            currentStatus = "LEARNING";
        }

        return VocabularyTopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .level(topic.getLevel())
                .status(currentStatus)
                .totalVocabularies((int) total)
                .masteredCount((int) mastered)
                .learningCount((int) learning)
                .newCount((int) newCount)
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .build();
    }
}
