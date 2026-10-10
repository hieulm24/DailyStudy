package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.it.CreateOrUpdateItNoteRequest;
import com.englishlearning.dto.it.ItNoteDto;
import com.englishlearning.entity.ItNote;
import com.englishlearning.entity.User;
import com.englishlearning.repository.ItNoteRepository;
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
import org.springframework.data.domain.Sort;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItNoteService {

    private final ItNoteRepository noteRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.it-notes-dir:uploads/it_notes}")
    private String itNotesUploadDir;

    private Path itNotesPath;

    @PostConstruct
    public void init() {
        try {
            itNotesPath = Paths.get(itNotesUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(itNotesPath);
        } catch (IOException e) {
            log.error("Could not initialize storage directory for IT notes images", e);
        }
    }

    public String uploadNoteImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File upload không được để trống");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "it_note_img.png");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedFilename = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.itNotesPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store note image", e);
            throw new RuntimeException("Lưu ảnh ghi chú thất bại: " + e.getMessage());
        }

        return "/api/it/notes/files/" + storedFilename;
    }

    public Resource loadNoteImageFile(String filename) {
        try {
            Path filePath = this.itNotesPath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File không tồn tại: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File không hợp lệ: " + filename);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ItNoteDto> getNotes(Long userId, String category, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isFavorite", "updatedAt"));
        Page<ItNote> notePage = noteRepository.searchNotes(userId, category, search, pageable);

        List<ItNoteDto> dtos = notePage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponse.<ItNoteDto>builder()
                .items(dtos)
                .page(notePage.getNumber())
                .size(notePage.getSize())
                .totalElements(notePage.getTotalElements())
                .totalPages(notePage.getTotalPages())
                .isFirst(notePage.isFirst())
                .isLast(notePage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public ItNoteDto getNoteById(Long userId, Long noteId) {
        ItNote note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ghi chú với ID: " + noteId));
        return mapToDto(note);
    }

    @Transactional
    public ItNoteDto createNote(Long userId, CreateOrUpdateItNoteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        ItNote note = ItNote.builder()
                .user(user)
                .title(request.getTitle().trim())
                .category(StringUtils.hasText(request.getCategory()) ? request.getCategory().trim() : "SYSTEM_DESIGN")
                .tags(request.getTags())
                .contentMarkdown(request.getContentMarkdown())
                .diagramMermaid(request.getDiagramMermaid())
                .imageUrlsJson(request.getImageUrlsJson())
                .isFavorite(Boolean.TRUE.equals(request.getIsFavorite()))
                .build();

        note = noteRepository.save(note);
        return mapToDto(note);
    }

    @Transactional
    public ItNoteDto updateNote(Long userId, Long noteId, CreateOrUpdateItNoteRequest request) {
        ItNote note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ghi chú với ID: " + noteId));

        note.setTitle(request.getTitle().trim());
        if (StringUtils.hasText(request.getCategory())) {
            note.setCategory(request.getCategory().trim());
        }
        note.setTags(request.getTags());
        note.setContentMarkdown(request.getContentMarkdown());
        note.setDiagramMermaid(request.getDiagramMermaid());
        note.setImageUrlsJson(request.getImageUrlsJson());
        if (request.getIsFavorite() != null) {
            note.setIsFavorite(request.getIsFavorite());
        }

        note = noteRepository.save(note);
        return mapToDto(note);
    }

    @Transactional
    public ItNoteDto toggleFavorite(Long userId, Long noteId) {
        ItNote note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ghi chú với ID: " + noteId));

        note.setIsFavorite(!Boolean.TRUE.equals(note.getIsFavorite()));
        note = noteRepository.save(note);
        return mapToDto(note);
    }

    @Transactional
    public void deleteNote(Long userId, Long noteId) {
        ItNote note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ghi chú với ID: " + noteId));
        noteRepository.delete(note);
    }

    private ItNoteDto mapToDto(ItNote note) {
        return ItNoteDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .category(note.getCategory())
                .tags(note.getTags())
                .contentMarkdown(note.getContentMarkdown())
                .diagramMermaid(note.getDiagramMermaid())
                .imageUrlsJson(note.getImageUrlsJson())
                .isFavorite(note.getIsFavorite())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
