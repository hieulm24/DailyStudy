package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.vocabulary.VocabularyExampleDto;
import com.englishlearning.dto.vocabulary.VocabularyFilterRequest;
import com.englishlearning.dto.vocabulary.VocabularyRequest;
import com.englishlearning.dto.vocabulary.VocabularyResponse;
import com.englishlearning.entity.*;
import com.englishlearning.repository.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final VocabularyTopicRepository vocabularyTopicRepository;
    private final VocabularyExampleRepository vocabularyExampleRepository;
    private final ReviewItemRepository reviewItemRepository;
    private final LearningActivityRepository learningActivityRepository;
    private final DailyLearningStatisticRepository dailyLearningStatisticRepository;
    private final StudyStreakRepository studyStreakRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<VocabularyResponse> getVocabularies(Long userId, VocabularyFilterRequest filter) {
        Specification<Vocabulary> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (filter.getTopicId() != null) {
                predicates.add(cb.equal(root.get("topic").get("id"), filter.getTopicId()));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("word")), keyword),
                        cb.like(cb.lower(root.get("meaning")), keyword)
                ));
            }

            if (StringUtils.hasText(filter.getLevel())) {
                predicates.add(cb.equal(root.get("level"), filter.getLevel()));
            }

            if (StringUtils.hasText(filter.getPartOfSpeech())) {
                predicates.add(cb.equal(root.get("partOfSpeech"), filter.getPartOfSpeech()));
            }

            if (StringUtils.hasText(filter.getStatus())) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Date filtering
            applyDateFilter(filter, root, cb, predicates);

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                filter.getSortBy() != null ? filter.getSortBy() : "createdAt"
        );

        int pageNum = filter.getPage() != null && filter.getPage() >= 0 ? filter.getPage() : 0;
        int pageSize = filter.getSize() != null && filter.getSize() > 0 ? filter.getSize() : 20;

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        Page<Vocabulary> page = vocabularyRepository.findAll(spec, pageable);

        List<VocabularyResponse> dtoList = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(page, dtoList);
    }

    @Transactional(readOnly = true)
    public VocabularyResponse getVocabularyById(Long id, Long userId) {
        Vocabulary vocabulary = vocabularyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng với id: " + id));
        return mapToResponse(vocabulary);
    }

    @Transactional
    public VocabularyResponse createVocabulary(Long userId, VocabularyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        VocabularyTopic topic = null;
        if (request.getTopicId() != null) {
            topic = vocabularyTopicRepository.findByIdAndUserId(request.getTopicId(), userId).orElse(null);
        }

        Vocabulary vocabulary = Vocabulary.builder()
                .user(user)
                .topic(topic)
                .word(request.getWord().trim())
                .meaning(request.getMeaning().trim())
                .pronunciation(request.getPronunciation() != null ? request.getPronunciation().trim() : null)
                .partOfSpeech(request.getPartOfSpeech())
                .level(request.getLevel())
                .note(request.getNote())
                .contextSentence(StringUtils.hasText(request.getContextSentence()) ? request.getContextSentence().trim() : null)
                .contextMeaning(StringUtils.hasText(request.getContextMeaning()) ? request.getContextMeaning().trim() : null)
                .status(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "NEW")
                .masteryLevel(0)
                .reviewCount(0)
                .nextReviewAt(LocalDateTime.now().plusDays(1))
                .build();

        Vocabulary saved = vocabularyRepository.save(vocabulary);

        // Add examples
        if (StringUtils.hasText(request.getExampleSentence())) {
            VocabularyExample example = VocabularyExample.builder()
                    .vocabulary(saved)
                    .exampleSentence(request.getExampleSentence().trim())
                    .meaning(request.getExampleMeaning() != null ? request.getExampleMeaning().trim() : null)
                    .isPrimary(true)
                    .build();
            saved.getExamples().add(example);
            vocabularyRepository.save(saved);
        } else if (request.getExamples() != null && !request.getExamples().isEmpty()) {
            for (VocabularyExampleDto exDto : request.getExamples()) {
                if (StringUtils.hasText(exDto.getExampleSentence())) {
                    VocabularyExample example = VocabularyExample.builder()
                            .vocabulary(saved)
                            .exampleSentence(exDto.getExampleSentence().trim())
                            .meaning(exDto.getMeaning())
                            .isPrimary(exDto.getIsPrimary() != null && exDto.getIsPrimary())
                            .build();
                    saved.getExamples().add(example);
                }
            }
            vocabularyRepository.save(saved);
        }

        // Create ReviewItem
        reviewItemRepository.save(ReviewItem.builder()
                .user(user)
                .contentType("VOCABULARY")
                .contentId(saved.getId())
                .reviewStatus("ACTIVE")
                .masteryLevel(0)
                .reviewCount(0)
                .currentIntervalDays(1)
                .nextReviewAt(LocalDateTime.now().plusDays(1))
                .build());

        // Create LearningActivity
        learningActivityRepository.save(LearningActivity.builder()
                .user(user)
                .activityType("ADD_VOCABULARY")
                .contentType("VOCABULARY")
                .contentId(saved.getId())
                .title("Đã thêm từ vựng: " + saved.getWord())
                .description(saved.getMeaning())
                .activityDate(LocalDateTime.now())
                .build());

        // Update Daily Stat & Streak
        updateDailyLearningStats(user, 1, 0, 0, 0);
        updateStudyStreak(user);

        return mapToResponse(saved);
    }

    @Transactional
    public VocabularyResponse updateVocabulary(Long id, Long userId, VocabularyRequest request) {
        Vocabulary vocabulary = vocabularyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng với id: " + id));

        vocabulary.setWord(request.getWord().trim());
        vocabulary.setMeaning(request.getMeaning().trim());
        vocabulary.setPronunciation(request.getPronunciation() != null ? request.getPronunciation().trim() : null);
        vocabulary.setPartOfSpeech(request.getPartOfSpeech());
        vocabulary.setLevel(request.getLevel());
        vocabulary.setNote(request.getNote());
        vocabulary.setContextSentence(StringUtils.hasText(request.getContextSentence()) ? request.getContextSentence().trim() : null);
        vocabulary.setContextMeaning(StringUtils.hasText(request.getContextMeaning()) ? request.getContextMeaning().trim() : null);
        if (StringUtils.hasText(request.getStatus())) {
            vocabulary.setStatus(request.getStatus());
        }

        if (request.getTopicId() != null) {
            VocabularyTopic topic = vocabularyTopicRepository.findByIdAndUserId(request.getTopicId(), userId)
                    .orElse(null);
            vocabulary.setTopic(topic);
        } else {
            vocabulary.setTopic(null);
        }

        vocabulary.getExamples().clear();

        if (StringUtils.hasText(request.getExampleSentence())) {
            VocabularyExample example = VocabularyExample.builder()
                    .vocabulary(vocabulary)
                    .exampleSentence(request.getExampleSentence().trim())
                    .meaning(request.getExampleMeaning())
                    .isPrimary(true)
                    .build();
            vocabulary.getExamples().add(example);
        } else if (request.getExamples() != null && !request.getExamples().isEmpty()) {
            for (VocabularyExampleDto exDto : request.getExamples()) {
                if (StringUtils.hasText(exDto.getExampleSentence())) {
                    VocabularyExample example = VocabularyExample.builder()
                            .vocabulary(vocabulary)
                            .exampleSentence(exDto.getExampleSentence().trim())
                            .meaning(exDto.getMeaning())
                            .isPrimary(exDto.getIsPrimary() != null && exDto.getIsPrimary())
                            .build();
                    vocabulary.getExamples().add(example);
                }
            }
        }

        Vocabulary saved = vocabularyRepository.save(vocabulary);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteVocabulary(Long id, Long userId) {
        Vocabulary vocabulary = vocabularyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng với id: " + id));

        reviewItemRepository.deleteByContentTypeAndContentId("VOCABULARY", id);
        vocabularyRepository.delete(vocabulary);
    }

    @Transactional
    public VocabularyResponse markAsMastered(Long id, Long userId) {
        Vocabulary vocabulary = vocabularyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng với id: " + id));

        vocabulary.setStatus("MASTERED");
        vocabulary.setMasteryLevel(5);
        vocabularyRepository.save(vocabulary);

        reviewItemRepository.findByUserIdAndContentTypeAndContentId(userId, "VOCABULARY", id)
                .ifPresent(r -> {
                    r.setReviewStatus("MASTERED");
                    r.setMasteryLevel(5);
                    reviewItemRepository.save(r);
                });

        return mapToResponse(vocabulary);
    }

    private void applyDateFilter(VocabularyFilterRequest filter, jakarta.persistence.criteria.Root<Vocabulary> root,
                                 jakarta.persistence.criteria.CriteriaBuilder cb, List<Predicate> predicates) {
        if (!StringUtils.hasText(filter.getDateRange())) {
            if (filter.getFromDate() != null && filter.getToDate() != null) {
                predicates.add(cb.between(root.get("createdAt"),
                        filter.getFromDate().atStartOfDay(),
                        filter.getToDate().atTime(LocalTime.MAX)));
            }
            return;
        }

        LocalDate today = LocalDate.now();
        switch (filter.getDateRange().toUpperCase()) {
            case "TODAY":
                predicates.add(cb.between(root.get("createdAt"), today.atStartOfDay(), today.atTime(LocalTime.MAX)));
                break;
            case "YESTERDAY":
                LocalDate yest = today.minusDays(1);
                predicates.add(cb.between(root.get("createdAt"), yest.atStartOfDay(), yest.atTime(LocalTime.MAX)));
                break;
            case "LAST_7_DAYS":
                predicates.add(cb.between(root.get("createdAt"), today.minusDays(7).atStartOfDay(), today.atTime(LocalTime.MAX)));
                break;
            case "LAST_30_DAYS":
                predicates.add(cb.between(root.get("createdAt"), today.minusDays(30).atStartOfDay(), today.atTime(LocalTime.MAX)));
                break;
            case "CUSTOM":
                if (filter.getFromDate() != null && filter.getToDate() != null) {
                    predicates.add(cb.between(root.get("createdAt"),
                            filter.getFromDate().atStartOfDay(),
                            filter.getToDate().atTime(LocalTime.MAX)));
                }
                break;
        }
    }

    private void updateDailyLearningStats(User user, int vocabInc, int grammarInc, int listeningInc, int speakingInc) {
        LocalDate today = LocalDate.now();
        DailyLearningStatistic stat = dailyLearningStatisticRepository.findByUserIdAndStatisticDate(user.getId(), today)
                .orElseGet(() -> DailyLearningStatistic.builder()
                        .user(user)
                        .statisticDate(today)
                        .build());

        stat.setVocabularyCount(stat.getVocabularyCount() + vocabInc);
        stat.setGrammarCount(stat.getGrammarCount() + grammarInc);
        stat.setListeningCount(stat.getListeningCount() + listeningInc);
        stat.setSpeakingCount(stat.getSpeakingCount() + speakingInc);
        stat.setTotalLearningCount(stat.getTotalLearningCount() + vocabInc + grammarInc + listeningInc + speakingInc);
        dailyLearningStatisticRepository.save(stat);
    }

    private void updateStudyStreak(User user) {
        LocalDate today = LocalDate.now();
        StudyStreak streak = studyStreakRepository.findByUserId(user.getId())
                .orElseGet(() -> StudyStreak.builder().user(user).build());

        if (streak.getLastStudyDate() == null) {
            streak.setCurrentStreak(1);
            streak.setLongestStreak(Math.max(1, streak.getLongestStreak()));
            streak.setLastStudyDate(today);
        } else if (streak.getLastStudyDate().equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            streak.setLongestStreak(Math.max(streak.getCurrentStreak(), streak.getLongestStreak()));
            streak.setLastStudyDate(today);
        } else if (!streak.getLastStudyDate().equals(today)) {
            streak.setCurrentStreak(1);
            streak.setLastStudyDate(today);
        }
        studyStreakRepository.save(streak);
    }

    private VocabularyResponse mapToResponse(Vocabulary vocab) {
        List<VocabularyExampleDto> examples = vocab.getExamples() != null
                ? vocab.getExamples().stream()
                .map(e -> VocabularyExampleDto.builder()
                        .id(e.getId())
                        .exampleSentence(e.getExampleSentence())
                        .meaning(e.getMeaning())
                        .isPrimary(e.getIsPrimary())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        return VocabularyResponse.builder()
                .id(vocab.getId())
                .word(vocab.getWord())
                .meaning(vocab.getMeaning())
                .pronunciation(vocab.getPronunciation())
                .partOfSpeech(vocab.getPartOfSpeech())
                .level(vocab.getLevel())
                .note(vocab.getNote())
                .contextSentence(vocab.getContextSentence())
                .contextMeaning(vocab.getContextMeaning())
                .status(vocab.getStatus())
                .topicId(vocab.getTopic() != null ? vocab.getTopic().getId() : null)
                .topicName(vocab.getTopic() != null ? vocab.getTopic().getName() : null)
                .masteryLevel(vocab.getMasteryLevel())
                .reviewCount(vocab.getReviewCount())
                .lastReviewedAt(vocab.getLastReviewedAt())
                .nextReviewAt(vocab.getNextReviewAt())
                .createdAt(vocab.getCreatedAt())
                .updatedAt(vocab.getUpdatedAt())
                .examples(examples)
                .build();
    }
}
