package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.task.*;
import com.englishlearning.entity.DailyTask;
import com.englishlearning.entity.LearningActivity;
import com.englishlearning.entity.User;
import com.englishlearning.repository.DailyTaskRepository;
import com.englishlearning.repository.LearningActivityRepository;
import com.englishlearning.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyTaskService {

    private final DailyTaskRepository dailyTaskRepository;
    private final UserRepository userRepository;
    private final LearningActivityRepository learningActivityRepository;

    private static final DateTimeFormatter VI_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional
    public DailyTaskResponse createTask(Long userId, DailyTaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng"));

        LocalDate date = request.getTaskDate() != null ? request.getTaskDate() : LocalDate.now();

        int order = request.getDisplayOrder() != null
                ? request.getDisplayOrder()
                : dailyTaskRepository.findMaxDisplayOrderByUserIdAndTaskDate(userId, date) + 1;

        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus().toUpperCase() : "PENDING";
        boolean isCompleted = "COMPLETED".equalsIgnoreCase(status);
        LocalDateTime completedAt = isCompleted ? LocalDateTime.now() : null;

        DailyTask task = DailyTask.builder()
                .user(user)
                .taskDate(date)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .category(StringUtils.hasText(request.getCategory()) ? request.getCategory().trim() : "GENERAL")
                .priority(StringUtils.hasText(request.getPriority()) ? request.getPriority().toUpperCase() : "MEDIUM")
                .status(status)
                .isCompleted(isCompleted)
                .displayOrder(order)
                .estimatedTime(request.getEstimatedTime())
                .completedAt(completedAt)
                .build();

        DailyTask saved = dailyTaskRepository.save(task);

        try {
            learningActivityRepository.save(LearningActivity.builder()
                    .user(user)
                    .activityType("ADD_TASK")
                    .contentType("DAILY_TASK")
                    .contentId(saved.getId())
                    .title("Đã thêm việc: " + saved.getTitle())
                    .description("Ngày: " + date.format(VI_DATE_FORMAT))
                    .activityDate(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Could not log activity for daily task", e);
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<DailyTaskResponse> getTasks(Long userId, DailyTaskFilterRequest filter) {
        Specification<DailyTask> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (filter.getTaskDate() != null) {
                predicates.add(cb.equal(root.get("taskDate"), filter.getTaskDate()));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(root.get("description")), keyword)
                ));
            }

            if (StringUtils.hasText(filter.getCategory())) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }

            if (StringUtils.hasText(filter.getPriority())) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority().toUpperCase()));
            }

            if (StringUtils.hasText(filter.getStatus())) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus().toUpperCase()));
            }

            if (filter.getIsCompleted() != null) {
                predicates.add(cb.equal(root.get("isCompleted"), filter.getIsCompleted()));
            }

            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("taskDate"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("taskDate"), filter.getToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                StringUtils.hasText(filter.getSortBy()) ? filter.getSortBy() : "displayOrder"
        );

        Pageable pageable = PageRequest.of(Math.max(0, filter.getPage()), Math.max(1, filter.getSize()), sort);
        Page<DailyTask> pageResult = dailyTaskRepository.findAll(spec, pageable);

        List<DailyTaskResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(pageResult, content);
    }

    @Transactional(readOnly = true)
    public List<DailyTaskResponse> getTasksByDate(Long userId, LocalDate taskDate) {
        LocalDate date = taskDate != null ? taskDate : LocalDate.now();
        List<DailyTask> tasks = dailyTaskRepository.findByUserIdAndTaskDateOrderByDisplayOrderAscCreatedAtAsc(userId, date);
        return tasks.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DailyTaskResponse getTaskById(Long id, Long userId) {
        DailyTask task = dailyTaskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc"));
        return mapToResponse(task);
    }

    @Transactional
    public DailyTaskResponse updateTask(Long id, Long userId, DailyTaskRequest request) {
        DailyTask task = dailyTaskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc"));

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        if (request.getTaskDate() != null) {
            task.setTaskDate(request.getTaskDate());
        }
        if (StringUtils.hasText(request.getCategory())) {
            task.setCategory(request.getCategory().trim());
        }
        if (StringUtils.hasText(request.getPriority())) {
            task.setPriority(request.getPriority().toUpperCase());
        }
        if (StringUtils.hasText(request.getStatus())) {
            String newStatus = request.getStatus().toUpperCase();
            task.setStatus(newStatus);
            if ("COMPLETED".equals(newStatus)) {
                task.setIsCompleted(true);
                if (task.getCompletedAt() == null) {
                    task.setCompletedAt(LocalDateTime.now());
                }
            } else {
                task.setIsCompleted(false);
                task.setCompletedAt(null);
            }
        }
        if (request.getDisplayOrder() != null) {
            task.setDisplayOrder(request.getDisplayOrder());
        }
        task.setEstimatedTime(request.getEstimatedTime());

        DailyTask updated = dailyTaskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional
    public DailyTaskResponse toggleTaskCompletion(Long id, Long userId) {
        DailyTask task = dailyTaskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc"));

        boolean willBeCompleted = !Boolean.TRUE.equals(task.getIsCompleted());
        task.setIsCompleted(willBeCompleted);
        if (willBeCompleted) {
            task.setStatus("COMPLETED");
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setStatus("PENDING");
            task.setCompletedAt(null);
        }

        DailyTask updated = dailyTaskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional
    public DailyTaskResponse updateTaskStatus(Long id, Long userId, String status) {
        DailyTask task = dailyTaskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc"));

        String upperStatus = status.toUpperCase();
        task.setStatus(upperStatus);
        if ("COMPLETED".equals(upperStatus)) {
            task.setIsCompleted(true);
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setIsCompleted(false);
            task.setCompletedAt(null);
        }

        DailyTask updated = dailyTaskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional
    public void reorderTasks(Long userId, LocalDate taskDate, List<DailyTaskReorderItem> items) {
        if (items == null || items.isEmpty()) return;

        List<DailyTask> tasks = dailyTaskRepository.findByUserIdAndTaskDateOrderByDisplayOrderAscCreatedAtAsc(userId, taskDate);
        Map<Long, DailyTask> taskMap = tasks.stream().collect(Collectors.toMap(DailyTask::getId, t -> t));

        for (DailyTaskReorderItem item : items) {
            DailyTask task = taskMap.get(item.getId());
            if (task != null && item.getDisplayOrder() != null) {
                task.setDisplayOrder(item.getDisplayOrder());
                dailyTaskRepository.save(task);
            }
        }
    }

    @Transactional
    public void deleteTask(Long id, Long userId) {
        DailyTask task = dailyTaskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc"));
        dailyTaskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public PageResponse<DailyTaskSummaryResponse> getDailySummaries(Long userId, LocalDate fromDate, LocalDate toDate, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<LocalDate> datePage;

        if (fromDate != null && toDate != null) {
            datePage = dailyTaskRepository.findDistinctTaskDatesByUserIdAndDateRange(userId, fromDate, toDate, pageable);
        } else {
            datePage = dailyTaskRepository.findDistinctTaskDatesByUserId(userId, pageable);
        }

        List<DailyTaskSummaryResponse> summaries = new ArrayList<>();

        for (LocalDate date : datePage.getContent()) {
            List<DailyTask> tasks = dailyTaskRepository.findByUserIdAndTaskDateOrderByDisplayOrderAscCreatedAtAsc(userId, date);
            long total = tasks.size();
            long completed = tasks.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
            long inProgress = tasks.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
            long pending = total - completed - inProgress;

            double rate = total > 0 ? Math.round(((double) completed / total * 100.0) * 10.0) / 10.0 : 0.0;

            String statusEvaluation;
            if (total == 0) {
                statusEvaluation = "NOT_STARTED";
            } else if (completed == total) {
                statusEvaluation = "COMPLETED_FULL"; // 100%
            } else if (completed > 0 || inProgress > 0) {
                statusEvaluation = "IN_PROGRESS";
            } else {
                statusEvaluation = "PENDING";
            }

            List<DailyTaskResponse> taskResponses = tasks.stream().map(this::mapToResponse).collect(Collectors.toList());

            summaries.add(DailyTaskSummaryResponse.builder()
                    .taskDate(date)
                    .dayOfWeek(formatVietnameseDayOfWeek(date))
                    .formattedDate(date.format(VI_DATE_FORMAT))
                    .totalTasks(total)
                    .completedTasks(completed)
                    .pendingTasks(Math.max(0, pending))
                    .inProgressTasks(inProgress)
                    .completionRate(rate)
                    .statusEvaluation(statusEvaluation)
                    .tasks(taskResponses)
                    .build());
        }

        return PageResponse.from(datePage, summaries);
    }

    @Transactional(readOnly = true)
    public DailyTaskStatsResponse getTaskStats(Long userId) {
        LocalDate today = LocalDate.now();
        List<DailyTask> todayTasks = dailyTaskRepository.findByUserIdAndTaskDateOrderByDisplayOrderAscCreatedAtAsc(userId, today);

        long todayTotal = todayTasks.size();
        long todayCompleted = todayTasks.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
        long todayInProgress = todayTasks.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long todayPending = todayTotal - todayCompleted - todayInProgress;
        double todayRate = todayTotal > 0 ? Math.round(((double) todayCompleted / todayTotal * 100.0) * 10.0) / 10.0 : 0.0;

        long allTotal = dailyTaskRepository.countByUserId(userId);
        long allCompleted = dailyTaskRepository.countByUserIdAndIsCompletedTrue(userId);
        double overallRate = allTotal > 0 ? Math.round(((double) allCompleted / allTotal * 100.0) * 10.0) / 10.0 : 0.0;

        // Group by category and priority
        List<DailyTask> allTasks = dailyTaskRepository.findAll((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));

        Map<String, Long> categoryCount = allTasks.stream()
                .filter(t -> StringUtils.hasText(t.getCategory()))
                .collect(Collectors.groupingBy(DailyTask::getCategory, Collectors.counting()));

        Map<String, Long> priorityCount = allTasks.stream()
                .filter(t -> StringUtils.hasText(t.getPriority()))
                .collect(Collectors.groupingBy(DailyTask::getPriority, Collectors.counting()));

        // Count days with 100% completion
        Map<LocalDate, List<DailyTask>> byDate = allTasks.stream().collect(Collectors.groupingBy(DailyTask::getTaskDate));
        long daysWithTasks = byDate.size();
        long daysFull = byDate.values().stream()
                .filter(list -> !list.isEmpty() && list.stream().allMatch(t -> Boolean.TRUE.equals(t.getIsCompleted())))
                .count();

        return DailyTaskStatsResponse.builder()
                .todayTotal(todayTotal)
                .todayCompleted(todayCompleted)
                .todayPending(Math.max(0, todayPending))
                .todayInProgress(todayInProgress)
                .todayCompletionRate(todayRate)
                .totalTasksAllTime(allTotal)
                .totalCompletedAllTime(allCompleted)
                .overallCompletionRate(overallRate)
                .totalDaysWithTasks(daysWithTasks)
                .totalDaysCompletedFull(daysFull)
                .countByCategory(categoryCount)
                .countByPriority(priorityCount)
                .build();
    }

    private DailyTaskResponse mapToResponse(DailyTask task) {
        return DailyTaskResponse.builder()
                .id(task.getId())
                .taskDate(task.getTaskDate())
                .title(task.getTitle())
                .description(task.getDescription())
                .category(task.getCategory())
                .priority(task.getPriority())
                .status(task.getStatus())
                .isCompleted(task.getIsCompleted())
                .displayOrder(task.getDisplayOrder())
                .estimatedTime(task.getEstimatedTime())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private String formatVietnameseDayOfWeek(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.equals(today)) {
            return "Hôm nay";
        } else if (date.equals(today.minusDays(1))) {
            return "Hôm qua";
        } else if (date.equals(today.plusDays(1))) {
            return "Ngày mai";
        }

        DayOfWeek dow = date.getDayOfWeek();
        return switch (dow) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
    }
}
