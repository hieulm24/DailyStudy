package com.englishlearning.service.vault;

import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.vault.*;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountVaultService {

    List<AccountCategoryDto> getCategories(Long userId);

    PageResponse<AccountCredentialDto> getCredentials(
            Long userId,
            Long categoryId,
            Boolean isFavorite,
            Boolean isDeleted,
            String search,
            int page,
            int size
    );

    List<AccountCredentialDto> getAllActiveCredentials(Long userId);

    AccountCredentialDetailDto getCredentialDetail(Long userId, Long id);

    AccountCredentialDetailDto createCredential(Long userId, CreateAccountCredentialRequest request);

    AccountCredentialDetailDto updateCredential(Long userId, Long id, UpdateAccountCredentialRequest request);

    void toggleFavorite(Long userId, Long id);

    void moveToTrash(Long userId, Long id);

    void restoreFromTrash(Long userId, Long id);

    void deletePermanently(Long userId, Long id);

    VaultSummaryDto getSummary(Long userId);

    PasswordGeneratorResponse generatePassword(PasswordGeneratorRequest request);

    AccountAttachmentDto uploadAttachment(Long userId, Long credentialId, MultipartFile file, String description);

    void deleteAttachment(Long userId, Long attachmentId);

    Resource loadAttachmentResource(Long userId, Long attachmentId);

    AccountAttachmentDto getAttachmentMetadata(Long userId, Long attachmentId);
}
