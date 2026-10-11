package com.englishlearning.repository.vault;

import com.englishlearning.entity.vault.AccountCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountCategoryRepository extends JpaRepository<AccountCategory, Long> {

    @Query("SELECT c FROM AccountCategory c WHERE c.isSystem = true OR c.user.id = :userId ORDER BY c.sortOrder ASC, c.id ASC")
    List<AccountCategory> findAllByUserIdOrSystem(@Param("userId") Long userId);

    boolean existsByUserIdAndCode(Long userId, String code);
}
