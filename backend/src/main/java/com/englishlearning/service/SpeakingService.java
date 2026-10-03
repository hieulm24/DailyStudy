package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.speaking.SpeakingFilterRequest;
import com.englishlearning.dto.speaking.SpeakingRequest;
import com.englishlearning.dto.speaking.SpeakingResponse;
import com.englishlearning.entity.DailyLearningStatistic;
import com.englishlearning.entity.LearningActivity;
import com.englishlearning.entity.SpeakingLesson;
import com.englishlearning.entity.StudyStreak;
import com.englishlearning.entity.User;
import com.englishlearning.repository.DailyLearningStatisticRepository;
import com.englishlearning.repository.LearningActivityRepository;
import com.englishlearning.repository.SpeakingLessonRepository;
import com.englishlearning.repository.StudyStreakRepository;
import com.englishlearning.repository.UserRepository;
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
public class SpeakingService {

    private final SpeakingLessonRepository speakingLessonRepository;
    private final LearningActivityRepository learningActivityRepository;
    private final DailyLearningStatisticRepository dailyLearningStatisticRepository;
    private final StudyStreakRepository studyStreakRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<SpeakingResponse> getSpeakingLessons(Long userId, SpeakingFilterRequest filter) {
        Specification<SpeakingLesson> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (StringUtils.hasText(filter.getSearch())) {
                String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(root.get("topic")), keyword),
                        cb.like(cb.lower(root.get("description")), keyword)
                ));
            }

            if (StringUtils.hasText(filter.getLevel())) {
                predicates.add(cb.equal(root.get("level"), filter.getLevel()));
            }

            if (StringUtils.hasText(filter.getStatus())) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

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
        Page<SpeakingLesson> page = speakingLessonRepository.findAll(spec, pageable);

        List<SpeakingResponse> dtoList = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(page, dtoList);
    }

    @Transactional(readOnly = true)
    public SpeakingResponse getSpeakingLessonById(Long id, Long userId) {
        SpeakingLesson lesson = speakingLessonRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài luyện nói với id: " + id));
        return mapToResponse(lesson);
    }

    @Transactional
    public SpeakingResponse createSpeakingLesson(Long userId, SpeakingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDateTime practicedTime = request.getPracticedAt() != null ? request.getPracticedAt() : LocalDateTime.now();

        SpeakingLesson lesson = SpeakingLesson.builder()
                .user(user)
                .title(request.getTitle().trim())
                .topic(request.getTopic())
                .description(request.getDescription())
                .url(request.getUrl())
                .durationSeconds(request.getDurationSeconds() != null ? request.getDurationSeconds() : 0)
                .level(request.getLevel())
                .note(request.getNote())
                .status(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "COMPLETED")
                .practiceCount(1)
                .practicedAt(practicedTime)
                .build();

        SpeakingLesson saved = speakingLessonRepository.save(lesson);

        learningActivityRepository.save(LearningActivity.builder()
                .user(user)
                .activityType("SPEAK")
                .contentType("SPEAKING")
                .contentId(saved.getId())
                .title("Luyện nói: " + saved.getTitle())
                .description(saved.getTopic())
                .durationSeconds(saved.getDurationSeconds())
                .activityDate(LocalDateTime.now())
                .build());

        updateDailyLearningStats(user, saved.getDurationSeconds() != null ? saved.getDurationSeconds() : 0);
        updateStudyStreak(user);

        return mapToResponse(saved);
    }

    @Transactional
    public SpeakingResponse updateSpeakingLesson(Long id, Long userId, SpeakingRequest request) {
        SpeakingLesson lesson = speakingLessonRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài luyện nói với id: " + id));

        lesson.setTitle(request.getTitle().trim());
        lesson.setTopic(request.getTopic());
        lesson.setDescription(request.getDescription());
        lesson.setUrl(request.getUrl());
        lesson.setDurationSeconds(request.getDurationSeconds());
        lesson.setLevel(request.getLevel());
        lesson.setNote(request.getNote());
        if (StringUtils.hasText(request.getStatus())) {
            lesson.setStatus(request.getStatus());
        }
        if (request.getPracticedAt() != null) {
            lesson.setPracticedAt(request.getPracticedAt());
        }

        SpeakingLesson saved = speakingLessonRepository.save(lesson);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteSpeakingLesson(Long id, Long userId) {
        SpeakingLesson lesson = speakingLessonRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài luyện nói với id: " + id));
        speakingLessonRepository.delete(lesson);
    }

    private void applyDateFilter(SpeakingFilterRequest filter, jakarta.persistence.criteria.Root<SpeakingLesson> root,
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

    private void updateDailyLearningStats(User user, int seconds) {
        LocalDate today = LocalDate.now();
        DailyLearningStatistic stat = dailyLearningStatisticRepository.findByUserIdAndStatisticDate(user.getId(), today)
                .orElseGet(() -> DailyLearningStatistic.builder()
                        .user(user)
                        .statisticDate(today)
                        .build());

        stat.setSpeakingCount(stat.getSpeakingCount() + 1);
        stat.setTotalLearningCount(stat.getTotalLearningCount() + 1);
        stat.setTotalLearningSeconds(stat.getTotalLearningSeconds() + seconds);
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

    private SpeakingResponse mapToResponse(SpeakingLesson lesson) {
        return SpeakingResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .topic(lesson.getTopic())
                .description(lesson.getDescription())
                .url(lesson.getUrl())
                .durationSeconds(lesson.getDurationSeconds())
                .level(lesson.getLevel())
                .note(lesson.getNote())
                .status(lesson.getStatus())
                .practiceCount(lesson.getPracticeCount())
                .practicedAt(lesson.getPracticedAt())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
