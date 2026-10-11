package com.englishlearning.repository.vault;

import com.englishlearning.entity.vault.AccountPasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountPasswordHistoryRepository extends JpaRepository<AccountPasswordHistory, Long> {

    List<AccountPasswordHistory> findByCredentialIdOrderByChangedAtDesc(Long credentialId);
}
