package com.englishlearning.service;

import com.englishlearning.dto.statistics.ActivityDto;
import com.englishlearning.entity.LearningActivity;
import com.englishlearning.repository.LearningActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final LearningActivityRepository learningActivityRepository;

    @Transactional(readOnly = true)
    public List<ActivityDto> getRecentActivities(Long userId, int limit) {
        return learningActivityRepository.findByUserIdOrderByActivityDateDesc(userId).stream()
                .limit(limit)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> getTodayActivities(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        return learningActivityRepository.findByUserIdAndActivityDateBetweenOrderByActivityDateDesc(userId, start, end).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ActivityDto mapToDto(LearningActivity a) {
        return ActivityDto.builder()
                .id(a.getId())
                .activityType(a.getActivityType())
                .contentType(a.getContentType())
                .contentId(a.getContentId())
                .title(a.getTitle())
                .description(a.getDescription())
                .activityDate(a.getActivityDate())
                .durationSeconds(a.getDurationSeconds())
                .build();
    }
}
