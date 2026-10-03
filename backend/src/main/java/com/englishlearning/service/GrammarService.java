package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.grammar.GrammarExampleDto;
import com.englishlearning.dto.grammar.GrammarFilterRequest;
import com.englishlearning.dto.grammar.GrammarRequest;
import com.englishlearning.dto.grammar.GrammarResponse;
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
public class GrammarService {

    private final GrammarTopicRepository grammarTopicRepository;
    private final GrammarExampleRepository grammarExampleRepository;
    private final ReviewItemRepository reviewItemRepository;
    private final LearningActivityRepository learningActivityRepository;
    private final DailyLearningStatisticRepository dailyLearningStatisticRepository;
    private final StudyStreakRepository studyStreakRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<GrammarResponse> getGrammars(Long userId, GrammarFilterRequest filter) {
        Specification<GrammarTopic> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (StringUtils.hasText(filter.getSearch())) {
                String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("topic")), keyword),
                        cb.like(cb.lower(root.get("structure")), keyword),
                        cb.like(cb.lower(root.get("usage")), keyword)
                ));
            }

            if (StringUtils.hasText(filter.getLevel())) {
                predicates.add(cb.equal(root.get("level"), filter.getLevel()));
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
        Page<GrammarTopic> page = grammarTopicRepository.findAll(spec, pageable);

        List<GrammarResponse> dtoList = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(page, dtoList);
    }

    @Transactional(readOnly = true)
    public GrammarResponse getGrammarById(Long id, Long userId) {
        GrammarTopic grammar = grammarTopicRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ đề ngữ pháp với id: " + id));
        return mapToResponse(grammar);
    }

    @Transactional
    public GrammarResponse createGrammar(Long userId, GrammarRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        GrammarTopic grammar = GrammarTopic.builder()
                .user(user)
                .topic(request.getTopic().trim())
                .level(request.getLevel())
                .structure(request.getStructure())
                .positiveStructure(request.getPositiveStructure())
                .negativeStructure(request.getNegativeStructure())
                .questionStructure(request.getQuestionStructure())
                .usage(request.getUsage())
                .signalWords(request.getSignalWords())
                .commonMistakes(request.getCommonMistakes())
                .note(request.getNote())
                .status(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "NEW")
                .masteryLevel(0)
                .reviewCount(0)
                .nextReviewAt(LocalDateTime.now().plusDays(1))
                .build();

        GrammarTopic saved = grammarTopicRepository.save(grammar);

        // Examples
        if (StringUtils.hasText(request.getExampleSentence())) {
            GrammarExample example = GrammarExample.builder()
                    .grammarTopic(saved)
                    .exampleSentence(request.getExampleSentence().trim())
                    .meaning(request.getExampleMeaning())
                    .isPrimary(true)
                    .build();
            saved.getExamples().add(example);
            grammarTopicRepository.save(saved);
        } else if (request.getExamples() != null && !request.getExamples().isEmpty()) {
            for (GrammarExampleDto exDto : request.getExamples()) {
                if (StringUtils.hasText(exDto.getExampleSentence())) {
                    GrammarExample example = GrammarExample.builder()
                            .grammarTopic(saved)
                            .exampleSentence(exDto.getExampleSentence().trim())
                            .meaning(exDto.getMeaning())
                            .isPrimary(exDto.getIsPrimary() != null && exDto.getIsPrimary())
                            .build();
                    saved.getExamples().add(example);
                }
            }
            grammarTopicRepository.save(saved);
        }

        // Review item
        reviewItemRepository.save(ReviewItem.builder()
                .user(user)
                .contentType("GRAMMAR")
                .contentId(saved.getId())
                .reviewStatus("ACTIVE")
                .masteryLevel(0)
                .reviewCount(0)
                .currentIntervalDays(1)
                .nextReviewAt(LocalDateTime.now().plusDays(1))
                .build());

        // Learning Activity
        learningActivityRepository.save(LearningActivity.builder()
                .user(user)
                .activityType("LEARN_GRAMMAR")
                .contentType("GRAMMAR")
                .contentId(saved.getId())
                .title("Đã học ngữ pháp: " + saved.getTopic())
                .description(saved.getStructure())
                .activityDate(LocalDateTime.now())
                .build());

        // Update stats and streak
        updateDailyLearningStats(user, 0, 1, 0, 0);
        updateStudyStreak(user);

        return mapToResponse(saved);
    }

    @Transactional
    public GrammarResponse updateGrammar(Long id, Long userId, GrammarRequest request) {
        GrammarTopic grammar = grammarTopicRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ đề ngữ pháp với id: " + id));

        grammar.setTopic(request.getTopic().trim());
        grammar.setLevel(request.getLevel());
        grammar.setStructure(request.getStructure());
        grammar.setPositiveStructure(request.getPositiveStructure());
        grammar.setNegativeStructure(request.getNegativeStructure());
        grammar.setQuestionStructure(request.getQuestionStructure());
        grammar.setUsage(request.getUsage());
        grammar.setSignalWords(request.getSignalWords());
        grammar.setCommonMistakes(request.getCommonMistakes());
        grammar.setNote(request.getNote());
        if (StringUtils.hasText(request.getStatus())) {
            grammar.setStatus(request.getStatus());
        }

        grammar.getExamples().clear();

        if (StringUtils.hasText(request.getExampleSentence())) {
            GrammarExample example = GrammarExample.builder()
                    .grammarTopic(grammar)
                    .exampleSentence(request.getExampleSentence().trim())
                    .meaning(request.getExampleMeaning())
                    .isPrimary(true)
                    .build();
            grammar.getExamples().add(example);
        } else if (request.getExamples() != null && !request.getExamples().isEmpty()) {
            for (GrammarExampleDto exDto : request.getExamples()) {
                if (StringUtils.hasText(exDto.getExampleSentence())) {
                    GrammarExample example = GrammarExample.builder()
                            .grammarTopic(grammar)
                            .exampleSentence(exDto.getExampleSentence().trim())
                            .meaning(exDto.getMeaning())
                            .isPrimary(exDto.getIsPrimary() != null && exDto.getIsPrimary())
                            .build();
                    grammar.getExamples().add(example);
                }
            }
        }

        GrammarTopic saved = grammarTopicRepository.save(grammar);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteGrammar(Long id, Long userId) {
        GrammarTopic grammar = grammarTopicRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ đề ngữ pháp với id: " + id));

        reviewItemRepository.deleteByContentTypeAndContentId("GRAMMAR", id);
        grammarTopicRepository.delete(grammar);
    }

    private void applyDateFilter(GrammarFilterRequest filter, jakarta.persistence.criteria.Root<GrammarTopic> root,
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

    private GrammarResponse mapToResponse(GrammarTopic topic) {
        List<GrammarExampleDto> examples = topic.getExamples() != null
                ? topic.getExamples().stream()
                .map(e -> GrammarExampleDto.builder()
                        .id(e.getId())
                        .exampleSentence(e.getExampleSentence())
                        .meaning(e.getMeaning())
                        .isPrimary(e.getIsPrimary())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        return GrammarResponse.builder()
                .id(topic.getId())
                .topic(topic.getTopic())
                .level(topic.getLevel())
                .structure(topic.getStructure())
                .positiveStructure(topic.getPositiveStructure())
                .negativeStructure(topic.getNegativeStructure())
                .questionStructure(topic.getQuestionStructure())
                .usage(topic.getUsage())
                .signalWords(topic.getSignalWords())
                .commonMistakes(topic.getCommonMistakes())
                .note(topic.getNote())
                .status(topic.getStatus())
                .masteryLevel(topic.getMasteryLevel())
                .reviewCount(topic.getReviewCount())
                .lastReviewedAt(topic.getLastReviewedAt())
                .nextReviewAt(topic.getNextReviewAt())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .examples(examples)
                .build();
    }
}
