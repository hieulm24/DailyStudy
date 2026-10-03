package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.listening.ListeningFilterRequest;
import com.englishlearning.dto.listening.ListeningRequest;
import com.englishlearning.dto.listening.ListeningResponse;
import com.englishlearning.entity.DailyLearningStatistic;
import com.englishlearning.entity.LearningActivity;
import com.englishlearning.entity.ListeningLesson;
import com.englishlearning.entity.StudyStreak;
import com.englishlearning.entity.User;
import com.englishlearning.repository.DailyLearningStatisticRepository;
import com.englishlearning.repository.LearningActivityRepository;
import com.englishlearning.repository.ListeningLessonRepository;
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
public class ListeningService {

    private final ListeningLessonRepository listeningLessonRepository;
    private final LearningActivityRepository learningActivityRepository;
    private final DailyLearningStatisticRepository dailyLearningStatisticRepository;
    private final StudyStreakRepository studyStreakRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<ListeningResponse> getListeningLessons(Long userId, ListeningFilterRequest filter) {
        Specification<ListeningLesson> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (StringUtils.hasText(filter.getSearch())) {
                String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
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
        Page<ListeningLesson> page = listeningLessonRepository.findAll(spec, pageable);

        List<ListeningResponse> dtoList = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(page, dtoList);
    }

    @Transactional(readOnly = true)
    public ListeningResponse getListeningLessonById(Long id, Long userId) {
        ListeningLesson lesson = listeningLessonRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nghe với id: " + id));
        return mapToResponse(lesson);
    }

    @Transactional
    public ListeningResponse createListeningLesson(Long userId, ListeningRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDateTime learnedTime = request.getLearnedAt() != null ? request.getLearnedAt() : LocalDateTime.now();

        ListeningLesson lesson = ListeningLesson.builder()
                .user(user)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .url(request.getUrl())
                .durationSeconds(request.getDurationSeconds() != null ? request.getDurationSeconds() : 0)
                .level(request.getLevel())
                .note(request.getNote())
                .status(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "COMPLETED")
                .listenedCount(1)
                .learnedAt(learnedTime)
                .build();

        ListeningLesson saved = listeningLessonRepository.save(lesson);

        learningActivityRepository.save(LearningActivity.builder()
                .user(user)
                .activityType("LISTEN")
                .contentType("LISTENING")
                .contentId(saved.getId())
                .title("Nghe bài: " + saved.getTitle())
                .description(saved.getDescription())
                .durationSeconds(saved.getDurationSeconds())
                .activityDate(LocalDateTime.now())
                .build());

        updateDailyLearningStats(user, saved.getDurationSeconds() != null ? saved.getDurationSeconds() : 0);
        updateStudyStreak(user);

        return mapToResponse(saved);
    }

    @Transactional
    public ListeningResponse updateListeningLesson(Long id, Long userId, ListeningRequest request) {
        ListeningLesson lesson = listeningLessonRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nghe với id: " + id));

        lesson.setTitle(request.getTitle().trim());
        lesson.setDescription(request.getDescription());
        lesson.setUrl(request.getUrl());
        lesson.setDurationSeconds(request.getDurationSeconds());
        lesson.setLevel(request.getLevel());
        lesson.setNote(request.getNote());
        if (StringUtils.hasText(request.getStatus())) {
            lesson.setStatus(request.getStatus());
        }
        if (request.getLearnedAt() != null) {
            lesson.setLearnedAt(request.getLearnedAt());
        }

        ListeningLesson saved = listeningLessonRepository.save(lesson);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteListeningLesson(Long id, Long userId) {
        ListeningLesson lesson = listeningLessonRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nghe với id: " + id));
        listeningLessonRepository.delete(lesson);
    }

    private void applyDateFilter(ListeningFilterRequest filter, jakarta.persistence.criteria.Root<ListeningLesson> root,
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

        stat.setListeningCount(stat.getListeningCount() + 1);
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

    private ListeningResponse mapToResponse(ListeningLesson lesson) {
        return ListeningResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .url(lesson.getUrl())
                .durationSeconds(lesson.getDurationSeconds())
                .level(lesson.getLevel())
                .note(lesson.getNote())
                .status(lesson.getStatus())
                .listenedCount(lesson.getListenedCount())
                .learnedAt(lesson.getLearnedAt())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
