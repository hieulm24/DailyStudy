package com.englishlearning.repository.vault;

import com.englishlearning.entity.vault.AccountCredentialAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountCredentialAttachmentRepository extends JpaRepository<AccountCredentialAttachment, Long> {

    List<AccountCredentialAttachment> findByCredentialIdOrderByCreatedAtDesc(Long credentialId);

    Optional<AccountCredentialAttachment> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
