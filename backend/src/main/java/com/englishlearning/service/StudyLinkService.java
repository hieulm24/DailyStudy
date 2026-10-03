package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.studylink.StudyLinkFilterRequest;
import com.englishlearning.dto.studylink.StudyLinkRequest;
import com.englishlearning.dto.studylink.StudyLinkResponse;
import com.englishlearning.entity.LearningActivity;
import com.englishlearning.entity.StudyLink;
import com.englishlearning.entity.User;
import com.englishlearning.repository.LearningActivityRepository;
import com.englishlearning.repository.StudyLinkRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyLinkService {

    private final StudyLinkRepository studyLinkRepository;
    private final UserRepository userRepository;
    private final LearningActivityRepository learningActivityRepository;

    @Transactional
    public StudyLinkResponse createLink(Long userId, StudyLinkRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng"));

        String safeUrl = request.getUrl().trim();
        if (!safeUrl.startsWith("http://") && !safeUrl.startsWith("https://")) {
            safeUrl = "https://" + safeUrl;
        }

        StudyLink link = StudyLink.builder()
                .user(user)
                .title(request.getTitle().trim())
                .url(safeUrl)
                .category(StringUtils.hasText(request.getCategory()) ? request.getCategory().trim() : "GENERAL")
                .description(request.getDescription())
                .clickCount(0)
                .isFavorite(Boolean.TRUE.equals(request.getIsFavorite()))
                .build();

        StudyLink saved = studyLinkRepository.save(link);

        try {
            learningActivityRepository.save(LearningActivity.builder()
                    .user(user)
                    .activityType("ADD_STUDY_LINK")
                    .contentType("STUDY_LINK")
                    .contentId(saved.getId())
                    .title("Đã lưu liên kết học tập: " + saved.getTitle())
                    .description(saved.getUrl())
                    .activityDate(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Could not log activity for study link creation", e);
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<StudyLinkResponse> getLinks(Long userId, StudyLinkFilterRequest filter) {
        Specification<StudyLink> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (StringUtils.hasText(filter.getSearch())) {
                String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(root.get("url")), keyword),
                        cb.like(cb.lower(root.get("description")), keyword)
                ));
            }

            if (StringUtils.hasText(filter.getCategory())) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }

            if (filter.getIsFavorite() != null) {
                predicates.add(cb.equal(root.get("isFavorite"), filter.getIsFavorite()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                StringUtils.hasText(filter.getSortBy()) ? filter.getSortBy() : "createdAt"
        );

        Pageable pageable = PageRequest.of(Math.max(0, filter.getPage()), Math.max(1, filter.getSize()), sort);
        Page<StudyLink> pageResult = studyLinkRepository.findAll(spec, pageable);

        List<StudyLinkResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(pageResult, content);
    }

    @Transactional(readOnly = true)
    public StudyLinkResponse getLinkById(Long id, Long userId) {
        StudyLink link = studyLinkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy liên kết học tập"));
        return mapToResponse(link);
    }

    @Transactional
    public StudyLinkResponse updateLink(Long id, Long userId, StudyLinkRequest request) {
        StudyLink link = studyLinkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy liên kết học tập"));

        String safeUrl = request.getUrl().trim();
        if (!safeUrl.startsWith("http://") && !safeUrl.startsWith("https://")) {
            safeUrl = "https://" + safeUrl;
        }

        link.setTitle(request.getTitle().trim());
        link.setUrl(safeUrl);
        if (StringUtils.hasText(request.getCategory())) {
            link.setCategory(request.getCategory().trim());
        }
        link.setDescription(request.getDescription());
        if (request.getIsFavorite() != null) {
            link.setIsFavorite(request.getIsFavorite());
        }

        StudyLink updated = studyLinkRepository.save(link);
        return mapToResponse(updated);
    }

    @Transactional
    public StudyLinkResponse recordClick(Long id, Long userId) {
        StudyLink link = studyLinkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy liên kết học tập"));

        link.setClickCount(link.getClickCount() + 1);
        StudyLink updated = studyLinkRepository.save(link);
        return mapToResponse(updated);
    }

    @Transactional
    public StudyLinkResponse toggleFavorite(Long id, Long userId) {
        StudyLink link = studyLinkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy liên kết học tập"));

        link.setIsFavorite(!Boolean.TRUE.equals(link.getIsFavorite()));
        StudyLink updated = studyLinkRepository.save(link);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteLink(Long id, Long userId) {
        StudyLink link = studyLinkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy liên kết học tập"));
        studyLinkRepository.delete(link);
    }

    private StudyLinkResponse mapToResponse(StudyLink link) {
        return StudyLinkResponse.builder()
                .id(link.getId())
                .title(link.getTitle())
                .url(link.getUrl())
                .category(link.getCategory())
                .description(link.getDescription())
                .clickCount(link.getClickCount())
                .isFavorite(link.getIsFavorite())
                .createdAt(link.getCreatedAt())
                .updatedAt(link.getUpdatedAt())
                .build();
    }
}
