package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.document.DocumentFilterRequest;
import com.englishlearning.dto.document.DocumentResponse;
import com.englishlearning.dto.document.DocumentStatisticsResponse;
import com.englishlearning.dto.document.DocumentUpdateRequest;
import com.englishlearning.entity.Document;
import com.englishlearning.entity.LearningActivity;
import com.englishlearning.entity.User;
import com.englishlearning.repository.DocumentRepository;
import com.englishlearning.repository.LearningActivityRepository;
import com.englishlearning.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final LearningActivityRepository learningActivityRepository;

    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.uploadPath);
            log.info("Document upload directory initialized at: {}", this.uploadPath);
        } catch (IOException e) {
            log.error("Could not initialize document storage directory", e);
            throw new RuntimeException("Could not initialize document storage directory", e);
        }
    }

    @Transactional
    public DocumentResponse uploadDocument(Long userId, MultipartFile file, String title, String category, String description) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn tệp tin cần tải lên");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng"));

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = getFileExtension(originalFileName);
        String fileType = determineFileType(fileExtension);
        String mimeType = file.getContentType();
        long fileSize = file.getSize();

        String displayTitle = StringUtils.hasText(title) ? title.trim() : originalFileName;
        String safeCategory = StringUtils.hasText(category) ? category.trim() : "GENERAL";

        String storedFileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        Path targetLocation = this.uploadPath.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store file: {}", originalFileName, e);
            throw new RuntimeException("Không thể lưu tệp tin lên máy chủ", e);
        }

        Document document = Document.builder()
                .user(user)
                .title(displayTitle)
                .fileName(originalFileName)
                .storedFileName(storedFileName)
                .filePath(targetLocation.toString())
                .fileType(fileType)
                .mimeType(mimeType)
                .fileSize(fileSize)
                .category(safeCategory)
                .description(description)
                .downloadCount(0)
                .isFavorite(false)
                .build();

        Document saved = documentRepository.save(document);

        // Record activity log
        try {
            learningActivityRepository.save(LearningActivity.builder()
                    .user(user)
                    .activityType("UPLOAD_DOCUMENT")
                    .contentType("DOCUMENT")
                    .contentId(saved.getId())
                    .title("Đã tải lên tài liệu: " + displayTitle)
                    .description("Tệp: " + originalFileName + " (" + formatFileSize(fileSize) + ")")
                    .activityDate(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Could not log activity for document upload", e);
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> getDocuments(Long userId, DocumentFilterRequest filter) {
        Specification<Document> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (StringUtils.hasText(filter.getSearch())) {
                String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(root.get("fileName")), keyword),
                        cb.like(cb.lower(root.get("description")), keyword)
                ));
            }

            if (StringUtils.hasText(filter.getGroup())) {
                if ("IT".equalsIgnoreCase(filter.getGroup())) {
                    predicates.add(cb.like(root.get("category"), "IT%"));
                } else if ("ENGLISH".equalsIgnoreCase(filter.getGroup())) {
                    predicates.add(cb.or(
                            cb.isNull(root.get("category")),
                            cb.notLike(root.get("category"), "IT%")
                    ));
                }
            }

            if (StringUtils.hasText(filter.getCategory())) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }

            if (StringUtils.hasText(filter.getFileType())) {
                predicates.add(cb.equal(root.get("fileType"), filter.getFileType()));
            }

            if (filter.getIsFavorite() != null) {
                predicates.add(cb.equal(root.get("isFavorite"), filter.getIsFavorite()));
            }

            if (StringUtils.hasText(filter.getDateRange())) {
                LocalDate now = LocalDate.now();
                switch (filter.getDateRange()) {
                    case "TODAY" -> predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), now.atStartOfDay()));
                    case "YESTERDAY" -> {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), now.minusDays(1).atStartOfDay()));
                        predicates.add(cb.lessThan(root.get("createdAt"), now.atStartOfDay()));
                    }
                    case "LAST_7_DAYS" -> predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), now.minusDays(7).atStartOfDay()));
                    case "LAST_30_DAYS" -> predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), now.minusDays(30).atStartOfDay()));
                    case "CUSTOM" -> {
                        if (filter.getFromDate() != null) {
                            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate().atStartOfDay()));
                        }
                        if (filter.getToDate() != null) {
                            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate().atTime(LocalTime.MAX)));
                        }
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                StringUtils.hasText(filter.getSortBy()) ? filter.getSortBy() : "createdAt"
        );

        Pageable pageable = PageRequest.of(Math.max(0, filter.getPage()), Math.max(1, filter.getSize()), sort);
        Page<Document> pageResult = documentRepository.findAll(spec, pageable);

        List<DocumentResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(pageResult, content);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id, Long userId) {
        Document document = documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu"));
        return mapToResponse(document);
    }

    @Transactional
    public Resource loadDocumentAsResource(Long id, Long userId) {
        Document document = documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu"));

        try {
            Path filePath = Paths.get(document.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                // Increment download count
                document.setDownloadCount(document.getDownloadCount() + 1);
                documentRepository.save(document);
                return resource;
            } else {
                throw new ResourceNotFoundException("Tệp tin không tồn tại trên hệ thống lưu trữ");
            }
        } catch (MalformedURLException e) {
            log.error("Invalid file path for document: {}", id, e);
            throw new ResourceNotFoundException("Không thể đọc tệp tin");
        }
    }

    @Transactional
    public DocumentResponse updateDocument(Long id, Long userId, DocumentUpdateRequest request) {
        Document document = documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu"));

        document.setTitle(request.getTitle().trim());
        if (StringUtils.hasText(request.getCategory())) {
            document.setCategory(request.getCategory().trim());
        }
        document.setDescription(request.getDescription());
        if (request.getIsFavorite() != null) {
            document.setIsFavorite(request.getIsFavorite());
        }

        Document updated = documentRepository.save(document);
        return mapToResponse(updated);
    }

    @Transactional
    public DocumentResponse toggleFavorite(Long id, Long userId) {
        Document document = documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu"));

        document.setIsFavorite(!Boolean.TRUE.equals(document.getIsFavorite()));
        Document updated = documentRepository.save(document);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteDocument(Long id, Long userId) {
        Document document = documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu"));

        // Delete physical file
        try {
            Path filePath = Paths.get(document.getFilePath()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete physical file: {}", document.getFilePath(), e);
        }

        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public DocumentStatisticsResponse getStatistics(Long userId) {
        long totalDocs = documentRepository.countByUserId(userId);
        Long totalBytes = documentRepository.sumFileSizeByUserId(userId);
        long excelCount = documentRepository.countByUserIdAndFileType(userId, "EXCEL");
        long wordCount = documentRepository.countByUserIdAndFileType(userId, "WORD");
        long pdfCount = documentRepository.countByUserIdAndFileType(userId, "PDF");
        long pptCount = documentRepository.countByUserIdAndFileType(userId, "POWERPOINT");
        long otherCount = totalDocs - (excelCount + wordCount + pdfCount + pptCount);

        List<Document> allDocs = documentRepository.findByUserId(userId);
        long favoriteCount = allDocs.stream().filter(d -> Boolean.TRUE.equals(d.getIsFavorite())).count();
        long itCount = allDocs.stream().filter(d -> d.getCategory() != null && d.getCategory().toUpperCase().startsWith("IT")).count();
        long englishCount = Math.max(0, totalDocs - itCount);

        Map<String, Long> categoryCount = allDocs.stream()
                .filter(d -> StringUtils.hasText(d.getCategory()))
                .collect(Collectors.groupingBy(Document::getCategory, Collectors.counting()));

        return DocumentStatisticsResponse.builder()
                .totalDocuments(totalDocs)
                .totalEnglish(englishCount)
                .totalIt(itCount)
                .totalFileSizeBytes(totalBytes != null ? totalBytes : 0L)
                .formattedTotalSize(formatFileSize(totalBytes != null ? totalBytes : 0L))
                .totalExcel(excelCount)
                .totalWord(wordCount)
                .totalPdf(pdfCount)
                .totalPowerPoint(pptCount)
                .totalOther(Math.max(0, otherCount))
                .totalFavorites(favoriteCount)
                .countByCategory(categoryCount)
                .build();
    }

    private DocumentResponse mapToResponse(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .fileName(doc.getFileName())
                .storedFileName(doc.getStoredFileName())
                .filePath(doc.getFilePath())
                .fileType(doc.getFileType())
                .mimeType(doc.getMimeType())
                .fileSize(doc.getFileSize())
                .formattedFileSize(formatFileSize(doc.getFileSize()))
                .category(doc.getCategory())
                .description(doc.getDescription())
                .downloadCount(doc.getDownloadCount())
                .isFavorite(doc.getIsFavorite())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String determineFileType(String ext) {
        return switch (ext) {
            case "xlsx", "xls", "csv" -> "EXCEL";
            case "docx", "doc" -> "WORD";
            case "pdf" -> "PDF";
            case "pptx", "ppt" -> "POWERPOINT";
            case "png", "jpg", "jpeg", "webp", "gif", "svg" -> "IMAGE";
            case "txt", "md", "json" -> "TEXT";
            default -> "OTHER";
        };
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, units.length - 1);
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
