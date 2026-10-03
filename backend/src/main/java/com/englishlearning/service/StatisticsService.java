package com.englishlearning.service;

import com.englishlearning.dto.statistics.ChartDataDto;
import com.englishlearning.dto.statistics.DailyStatDto;
import com.englishlearning.dto.statistics.DashboardSummaryDto;
import com.englishlearning.dto.statistics.StreakHeatmapDto;
import com.englishlearning.entity.DailyLearningStatistic;
import com.englishlearning.entity.StudyStreak;
import com.englishlearning.entity.UserSetting;
import com.englishlearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final DailyLearningStatisticRepository dailyLearningStatisticRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GrammarTopicRepository grammarTopicRepository;
    private final ListeningLessonRepository listeningLessonRepository;
    private final SpeakingLessonRepository speakingLessonRepository;
    private final ReviewItemRepository reviewItemRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final GameSessionRepository gameSessionRepository;
    private final StudyStreakRepository studyStreakRepository;
    private final UserSettingRepository userSettingRepository;
    private final ActivityService activityService;

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // Today counts
        long todayVocab = vocabularyRepository.countByUserIdAndCreatedAtBetween(userId, todayStart, todayEnd);
        long todayGrammar = grammarTopicRepository.countByUserIdAndCreatedAtBetween(userId, todayStart, todayEnd);
        long todayListening = listeningLessonRepository.countByUserIdAndCreatedAtBetween(userId, todayStart, todayEnd);
        long todaySpeaking = speakingLessonRepository.countByUserIdAndCreatedAtBetween(userId, todayStart, todayEnd);
        long todayReviews = reviewHistoryRepository.countByUserIdAndReviewedAtBetween(userId, todayStart, todayEnd);

        int todayTotal = (int) (todayVocab + todayGrammar + todayListening + todaySpeaking);

        // Reviews due
        long needReview = reviewItemRepository.countDueReviewItems(userId, LocalDateTime.now());

        // Target
        int dailyTarget = userSettingRepository.findByUserId(userId)
                .map(UserSetting::getDailyLearningTarget)
                .orElse(30);

        int progressPercent = (int) Math.min(100, Math.round(((double) (todayTotal + todayReviews) / Math.max(1, dailyTarget)) * 100));

        // Streak
        StudyStreak streak = studyStreakRepository.findByUserId(userId)
                .orElseGet(() -> StudyStreak.builder().currentStreak(0).longestStreak(0).build());

        // Overalls
        long totalVocab = vocabularyRepository.countByUserId(userId);
        long totalGrammar = grammarTopicRepository.countByUserId(userId);
        long totalListening = listeningLessonRepository.countByUserId(userId);
        long totalSpeaking = speakingLessonRepository.countByUserId(userId);
        long totalReviews = reviewHistoryRepository.countByUserId(userId);
        long totalGames = gameSessionRepository.countByUserId(userId);

        return DashboardSummaryDto.builder()
                .todayVocabulary((int) todayVocab)
                .todayGrammar((int) todayGrammar)
                .todayListening((int) todayListening)
                .todaySpeaking((int) todaySpeaking)
                .todayTotal(todayTotal)
                .todayNeedReview(needReview)
                .todayCompletedReview(todayReviews)
                .todayProgressPercent(progressPercent)
                .dailyLearningTarget(dailyTarget)
                .totalVocabulary(totalVocab)
                .totalGrammar(totalGrammar)
                .totalListening(totalListening)
                .totalSpeaking(totalSpeaking)
                .totalReviews(totalReviews)
                .totalGameSessions(totalGames)
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .lastStudyDate(streak.getLastStudyDate())
                .recentActivities(activityService.getRecentActivities(userId, 6))
                .build();
    }

    @Transactional(readOnly = true)
    public ChartDataDto getChartData(Long userId, String range, LocalDate fromDate, LocalDate toDate) {
        LocalDate end = LocalDate.now();
        LocalDate start;

        if ("LAST_30_DAYS".equalsIgnoreCase(range)) {
            start = end.minusDays(29);
        } else if ("THIS_MONTH".equalsIgnoreCase(range)) {
            start = end.withDayOfMonth(1);
        } else if ("CUSTOM".equalsIgnoreCase(range) && fromDate != null && toDate != null) {
            start = fromDate;
            end = toDate;
        } else {
            // Default 7 days
            start = end.minusDays(6);
        }

        List<DailyLearningStatistic> stats = dailyLearningStatisticRepository
                .findByUserIdAndStatisticDateBetweenOrderByStatisticDateAsc(userId, start, end);

        Map<LocalDate, DailyLearningStatistic> statMap = stats.stream()
                .collect(Collectors.toMap(DailyLearningStatistic::getStatisticDate, s -> s));

        List<String> labels = new ArrayList<>();
        List<Integer> vocabData = new ArrayList<>();
        List<Integer> grammarData = new ArrayList<>();
        List<Integer> listeningData = new ArrayList<>();
        List<Integer> speakingData = new ArrayList<>();
        List<Integer> reviewData = new ArrayList<>();
        List<Integer> totalData = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            labels.add(cur.format(formatter));
            DailyLearningStatistic s = statMap.get(cur);

            int v = s != null ? s.getVocabularyCount() : 0;
            int g = s != null ? s.getGrammarCount() : 0;
            int l = s != null ? s.getListeningCount() : 0;
            int sp = s != null ? s.getSpeakingCount() : 0;
            int r = s != null ? s.getReviewCount() : 0;
            int t = v + g + l + sp + r;

            vocabData.add(v);
            grammarData.add(g);
            listeningData.add(l);
            speakingData.add(sp);
            reviewData.add(r);
            totalData.add(t);

            cur = cur.plusDays(1);
        }

        return ChartDataDto.builder()
                .labels(labels)
                .vocabularyData(vocabData)
                .grammarData(grammarData)
                .listeningData(listeningData)
                .speakingData(speakingData)
                .reviewData(reviewData)
                .totalData(totalData)
                .build();
    }

    @Transactional(readOnly = true)
    public StreakHeatmapDto getStreakAndHeatmap(Long userId) {
        StudyStreak streak = studyStreakRepository.findByUserId(userId)
                .orElseGet(() -> StudyStreak.builder().currentStreak(0).longestStreak(0).build());

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(90); // 3 months heatmap

        List<DailyLearningStatistic> stats = dailyLearningStatisticRepository
                .findByUserIdAndStatisticDateBetweenOrderByStatisticDateAsc(userId, start, end);

        Map<LocalDate, Integer> countMap = new HashMap<>();
        for (DailyLearningStatistic s : stats) {
            int total = s.getTotalLearningCount() + s.getReviewCount();
            countMap.put(s.getStatisticDate(), total);
        }

        List<StreakHeatmapDto.HeatmapDayDto> heatmapDays = new ArrayList<>();
        LocalDate cur = start;
        long totalStudyDays = 0;

        while (!cur.isAfter(end)) {
            int count = countMap.getOrDefault(cur, 0);
            if (count > 0) totalStudyDays++;

            int level = 0;
            if (count >= 10) level = 4;
            else if (count >= 6) level = 3;
            else if (count >= 3) level = 2;
            else if (count >= 1) level = 1;

            heatmapDays.add(StreakHeatmapDto.HeatmapDayDto.builder()
                    .date(cur.toString())
                    .count(count)
                    .level(level)
                    .build());

            cur = cur.plusDays(1);
        }

        return StreakHeatmapDto.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .lastStudyDate(streak.getLastStudyDate())
                .totalStudyDays(totalStudyDays)
                .heatmapDays(heatmapDays)
                .build();
    }
}
