package com.englishlearning.service;

import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.review.ReviewDueSummaryResponse;
import com.englishlearning.dto.review.ReviewItemDto;
import com.englishlearning.dto.review.ReviewSubmitRequest;
import com.englishlearning.dto.review.TopicReviewSummaryDto;
import com.englishlearning.entity.*;
import com.englishlearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewItemRepository reviewItemRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final VocabularyRepository vocabularyRepository;
    private final VocabularyTopicRepository vocabularyTopicRepository;
    private final GrammarTopicRepository grammarTopicRepository;
    private final LearningActivityRepository learningActivityRepository;
    private final DailyLearningStatisticRepository dailyLearningStatisticRepository;
    private final StudyStreakRepository studyStreakRepository;

    @Transactional(readOnly = true)
    public ReviewDueSummaryResponse getReviewSummary(Long userId, Long topicId) {
        LocalDateTime now = LocalDateTime.now();

        if (topicId != null) {
            long vocabDue = reviewItemRepository.countDueByTopicId(userId, topicId, now);
            long totalTopicWords = vocabularyTopicRepository.countVocabulariesByTopicId(topicId);
            long masteredTopicWords = vocabularyTopicRepository.countVocabulariesByTopicIdAndStatus(topicId, "MASTERED");

            return ReviewDueSummaryResponse.builder()
                    .totalDue(vocabDue)
                    .vocabularyDue(vocabDue)
                    .grammarDue(0L)
                    .totalItems(totalTopicWords)
                    .totalMastered(masteredTopicWords)
                    .build();
        }

        long vocabDue = reviewItemRepository.countDueByContentType(userId, "VOCABULARY", now);
        long grammarDue = reviewItemRepository.countDueByContentType(userId, "GRAMMAR", now);
        long totalDue = vocabDue + grammarDue;

        long totalItems = reviewItemRepository.findByUserId(userId).size();
        long totalMastered = reviewItemRepository.findByUserId(userId).stream()
                .filter(r -> "MASTERED".equalsIgnoreCase(r.getReviewStatus()) || r.getMasteryLevel() >= 5)
                .count();

        return ReviewDueSummaryResponse.builder()
                .totalDue(totalDue)
                .vocabularyDue(vocabDue)
                .grammarDue(grammarDue)
                .totalItems(totalItems)
                .totalMastered(totalMastered)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TopicReviewSummaryDto> getTopicsReviewSummary(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<VocabularyTopic> topics = vocabularyTopicRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<TopicReviewSummaryDto> list = new ArrayList<>();

        for (VocabularyTopic topic : topics) {
            long total = vocabularyTopicRepository.countVocabulariesByTopicId(topic.getId());
            long due = reviewItemRepository.countDueByTopicId(userId, topic.getId(), now);
            long mastered = vocabularyTopicRepository.countVocabulariesByTopicIdAndStatus(topic.getId(), "MASTERED");
            long learning = vocabularyTopicRepository.countVocabulariesByTopicIdAndStatus(topic.getId(), "LEARNING");

            list.add(TopicReviewSummaryDto.builder()
                    .topicId(topic.getId())
                    .topicName(topic.getName())
                    .level(topic.getLevel())
                    .status(topic.getStatus())
                    .totalWords((int) total)
                    .dueWords((int) due)
                    .masteredWords((int) mastered)
                    .learningWords((int) learning)
                    .build());
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<ReviewItemDto> getDueReviewItems(Long userId, Long topicId) {
        LocalDateTime now = LocalDateTime.now();
        List<ReviewItem> items;

        if (topicId != null) {
            items = reviewItemRepository.findDueReviewItemsByTopic(userId, topicId, now);
            if (items.isEmpty()) {
                // If no items are due according to interval, load all words from this topic so user can still practice
                items = reviewItemRepository.findAllReviewItemsByTopic(userId, topicId);
            }
        } else {
            items = reviewItemRepository.findDueReviewItems(userId, now);
        }

        List<ReviewItemDto> result = new ArrayList<>();

        for (ReviewItem item : items) {
            ReviewItemDto.ReviewItemDtoBuilder builder = ReviewItemDto.builder()
                    .id(item.getId())
                    .contentType(item.getContentType())
                    .contentId(item.getContentId())
                    .masteryLevel(item.getMasteryLevel())
                    .reviewCount(item.getReviewCount())
                    .currentIntervalDays(item.getCurrentIntervalDays())
                    .lastReviewedAt(item.getLastReviewedAt())
                    .nextReviewAt(item.getNextReviewAt());

            if ("VOCABULARY".equalsIgnoreCase(item.getContentType())) {
                Optional<Vocabulary> vOpt = vocabularyRepository.findById(item.getContentId());
                if (vOpt.isPresent()) {
                    Vocabulary v = vOpt.get();
                    builder.title(v.getWord())
                            .subtitle(v.getPronunciation() != null ? v.getPronunciation() : v.getPartOfSpeech())
                            .primaryMeaning(v.getMeaning())
                            .note(v.getNote());

                    if (v.getExamples() != null && !v.getExamples().isEmpty()) {
                        VocabularyExample ex = v.getExamples().get(0);
                        builder.exampleSentence(ex.getExampleSentence())
                                .exampleMeaning(ex.getMeaning());
                    }
                    result.add(builder.build());
                }
            } else if ("GRAMMAR".equalsIgnoreCase(item.getContentType())) {
                Optional<GrammarTopic> gOpt = grammarTopicRepository.findById(item.getContentId());
                if (gOpt.isPresent()) {
                    GrammarTopic g = gOpt.get();
                    builder.title(g.getTopic())
                            .subtitle(g.getLevel())
                            .structure(g.getStructure())
                            .primaryMeaning(g.getUsage())
                            .note(g.getNote());

                    if (g.getExamples() != null && !g.getExamples().isEmpty()) {
                        GrammarExample ex = g.getExamples().get(0);
                        builder.exampleSentence(ex.getExampleSentence())
                                .exampleMeaning(ex.getMeaning());
                    }
                    result.add(builder.build());
                }
            }
        }

        return result;
    }

    @Transactional
    public ReviewItemDto submitReview(Long userId, ReviewSubmitRequest request) {
        ReviewItem item = reviewItemRepository.findById(request.getReviewItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục ôn tập với id: " + request.getReviewItemId()));

        if (!item.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Không có quyền ôn tập mục này");
        }

        int prevMastery = item.getMasteryLevel() != null ? item.getMasteryLevel() : 0;
        int prevInterval = item.getCurrentIntervalDays() != null && item.getCurrentIntervalDays() > 0 ? item.getCurrentIntervalDays() : 1;

        int newMastery;
        int newInterval;

        String result = request.getResult().toUpperCase();
        switch (result) {
            case "FORGOT":
                newInterval = 1;
                newMastery = Math.max(0, prevMastery - 1);
                break;
            case "HARD":
                newInterval = Math.max(1, (int) Math.round(prevInterval * 1.2));
                newMastery = prevMastery;
                break;
            case "GOOD":
                if (prevInterval <= 1) {
                    newInterval = 3;
                } else if (prevInterval <= 3) {
                    newInterval = 7;
                } else {
                    newInterval = Math.max(prevInterval + 1, prevInterval * 2);
                }
                newMastery = Math.min(5, prevMastery + 1);
                break;
            case "EASY":
                newInterval = Math.max(4, (int) Math.round(prevInterval * 2.5));
                newMastery = Math.min(5, prevMastery + 2);
                break;
            default:
                newInterval = 1;
                newMastery = prevMastery;
                break;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReview = now.plusDays(newInterval);

        // Update ReviewItem
        item.setLastReviewedAt(now);
        item.setNextReviewAt(nextReview);
        item.setCurrentIntervalDays(newInterval);
        item.setMasteryLevel(newMastery);
        item.setReviewCount((item.getReviewCount() != null ? item.getReviewCount() : 0) + 1);
        if (newMastery >= 5) {
            item.setReviewStatus("MASTERED");
        } else {
            item.setReviewStatus("ACTIVE");
        }
        reviewItemRepository.save(item);

        // Save History
        ReviewHistory history = ReviewHistory.builder()
                .reviewItem(item)
                .user(item.getUser())
                .result(result)
                .difficulty(request.getDifficulty())
                .previousMasteryLevel(prevMastery)
                .newMasteryLevel(newMastery)
                .previousIntervalDays(prevInterval)
                .newIntervalDays(newInterval)
                .reviewedAt(now)
                .build();
        reviewHistoryRepository.save(history);

        // Sync with underlying entity & activity
        String itemTitle = "";
        if ("VOCABULARY".equalsIgnoreCase(item.getContentType())) {
            Optional<Vocabulary> vOpt = vocabularyRepository.findById(item.getContentId());
            if (vOpt.isPresent()) {
                Vocabulary v = vOpt.get();
                v.setLastReviewedAt(now);
                v.setNextReviewAt(nextReview);
                v.setMasteryLevel(newMastery);
                v.setReviewCount((v.getReviewCount() != null ? v.getReviewCount() : 0) + 1);
                if (newMastery >= 5) {
                    v.setStatus("MASTERED");
                } else {
                    v.setStatus("LEARNING");
                }
                vocabularyRepository.save(v);
                itemTitle = v.getWord();
            }
        } else if ("GRAMMAR".equalsIgnoreCase(item.getContentType())) {
            Optional<GrammarTopic> gOpt = grammarTopicRepository.findById(item.getContentId());
            if (gOpt.isPresent()) {
                GrammarTopic g = gOpt.get();
                g.setLastReviewedAt(now);
                g.setNextReviewAt(nextReview);
                g.setMasteryLevel(newMastery);
                g.setReviewCount((g.getReviewCount() != null ? g.getReviewCount() : 0) + 1);
                if (newMastery >= 5) {
                    g.setStatus("MASTERED");
                } else {
                    g.setStatus("LEARNING");
                }
                grammarTopicRepository.save(g);
                itemTitle = g.getTopic();
            }
        }

        // Learning Activity
        learningActivityRepository.save(LearningActivity.builder()
                .user(item.getUser())
                .activityType("REVIEW")
                .contentType(item.getContentType())
                .contentId(item.getContentId())
                .title("Ôn tập: " + itemTitle)
                .description("Đánh giá: " + result + " -> Cách " + newInterval + " ngày ôn tiếp")
                .activityDate(now)
                .build());

        // Update stats and streak
        updateDailyLearningStats(item.getUser());
        updateStudyStreak(item.getUser());

        return ReviewItemDto.builder()
                .id(item.getId())
                .contentType(item.getContentType())
                .contentId(item.getContentId())
                .masteryLevel(newMastery)
                .reviewCount(item.getReviewCount())
                .currentIntervalDays(newInterval)
                .lastReviewedAt(now)
                .nextReviewAt(nextReview)
                .title(itemTitle)
                .build();
    }

    private void updateDailyLearningStats(User user) {
        LocalDate today = LocalDate.now();
        DailyLearningStatistic stat = dailyLearningStatisticRepository.findByUserIdAndStatisticDate(user.getId(), today)
                .orElseGet(() -> DailyLearningStatistic.builder()
                        .user(user)
                        .statisticDate(today)
                        .build());

        stat.setReviewCount(stat.getReviewCount() + 1);
        stat.setTotalLearningCount(stat.getTotalLearningCount() + 1);
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
}
