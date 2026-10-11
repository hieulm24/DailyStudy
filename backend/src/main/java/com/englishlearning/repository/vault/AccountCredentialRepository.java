package com.englishlearning.repository.vault;

import com.englishlearning.entity.vault.AccountCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountCredentialRepository extends JpaRepository<AccountCredential, Long> {

    Optional<AccountCredential> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM AccountCredential c " +
           "WHERE c.user.id = :userId " +
           "AND (:isDeleted IS NULL OR c.isDeleted = :isDeleted) " +
           "AND (:categoryId IS NULL OR c.category.id = :categoryId) " +
           "AND (:isFavorite IS NULL OR c.isFavorite = :isFavorite) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(c.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(c.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY c.isFavorite DESC, c.updatedAt DESC")
    Page<AccountCredential> findWithFilters(
            @Param("userId") Long userId,
            @Param("isDeleted") Boolean isDeleted,
            @Param("categoryId") Long categoryId,
            @Param("isFavorite") Boolean isFavorite,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByUserIdAndIsDeletedFalse(Long userId);

    long countByUserIdAndIsFavoriteTrueAndIsDeletedFalse(Long userId);

    long countByUserIdAndPasswordStrengthAndIsDeletedFalse(Long userId, String passwordStrength);

    long countByUserIdAndIsDeletedTrue(Long userId);

    List<AccountCredential> findByUserIdAndIsDeletedFalseOrderByTitleAsc(Long userId);
}
