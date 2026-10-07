package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.health.HealthWorkoutPhotoRequest;
import com.englishlearning.dto.health.HealthWorkoutPhotoResponse;
import com.englishlearning.entity.HealthWorkoutPhoto;
import com.englishlearning.entity.User;
import com.englishlearning.repository.HealthWorkoutPhotoRepository;
import com.englishlearning.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthWorkoutPhotoService {

    private final HealthWorkoutPhotoRepository photoRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.photos-dir:uploads/photos}")
    private String photosUploadDir;

    private Path photosPath;

    @PostConstruct
    public void init() {
        try {
            photosPath = Paths.get(photosUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(photosPath);
        } catch (IOException e) {
            log.error("Could not initialize storage directory for workout photos", e);
        }
    }

    @Transactional
    public HealthWorkoutPhotoResponse savePhoto(Long userId, HealthWorkoutPhotoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        HealthWorkoutPhoto photo = HealthWorkoutPhoto.builder()
                .user(user)
                .logDate(request.getLogDate() != null ? request.getLogDate() : LocalDate.now())
                .imageUrl(request.getImageUrl())
                .caption(request.getCaption())
                .weightKg(request.getWeightKg())
                .build();

        HealthWorkoutPhoto saved = photoRepository.save(photo);
        return mapToResponse(saved);
    }

    @Transactional
    public HealthWorkoutPhotoResponse uploadAndSavePhoto(
            Long userId,
            MultipartFile file,
            LocalDate logDate,
            String caption,
            BigDecimal weightKg
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File upload không được để trống");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "photo.jpg");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedFilename = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.photosPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store workout photo", e);
            throw new RuntimeException("Lưu ảnh thất bại: " + e.getMessage());
        }

        String imageUrl = "/api/health/photos/files/" + storedFilename;

        HealthWorkoutPhoto photo = HealthWorkoutPhoto.builder()
                .user(user)
                .logDate(logDate != null ? logDate : LocalDate.now())
                .imageUrl(imageUrl)
                .caption(caption)
                .weightKg(weightKg)
                .build();

        HealthWorkoutPhoto saved = photoRepository.save(photo);
        return mapToResponse(saved);
    }

    public Resource loadPhotoFile(String filename) {
        try {
            Path filePath = this.photosPath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File không tồn tại: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File không hợp lệ: " + filename);
        }
    }

    @Transactional(readOnly = true)
    public List<HealthWorkoutPhotoResponse> getPhotosByDate(Long userId, LocalDate logDate) {
        LocalDate targetDate = logDate != null ? logDate : LocalDate.now();
        return photoRepository.findByUserIdAndLogDateOrderByCreatedAtDesc(userId, targetDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<HealthWorkoutPhotoResponse> getAllPhotos(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HealthWorkoutPhoto> photoPage = photoRepository.findByUserIdOrderByLogDateDescCreatedAtDesc(userId, pageable);

        List<HealthWorkoutPhotoResponse> content = photoPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(photoPage, content);
    }

    @Transactional
    public void deletePhoto(Long id, Long userId) {
        HealthWorkoutPhoto photo = photoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + id));
        photoRepository.delete(photo);
    }

    private HealthWorkoutPhotoResponse mapToResponse(HealthWorkoutPhoto photo) {
        return HealthWorkoutPhotoResponse.builder()
                .id(photo.getId())
                .userId(photo.getUser() != null ? photo.getUser().getId() : null)
                .logDate(photo.getLogDate())
                .imageUrl(photo.getImageUrl())
                .caption(photo.getCaption())
                .weightKg(photo.getWeightKg())
                .createdAt(photo.getCreatedAt())
                .updatedAt(photo.getUpdatedAt())
                .build();
    }
}
