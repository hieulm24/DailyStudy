package com.englishlearning.repository;

import com.englishlearning.entity.ItCodeSnippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItCodeSnippetRepository extends JpaRepository<ItCodeSnippet, Long> {

    List<ItCodeSnippet> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<ItCodeSnippet> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT s FROM ItCodeSnippet s WHERE s.user.id = :userId AND " +
           "(:language IS NULL OR :language = '' OR s.language = :language) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(s.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.codeContent) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.tags) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY s.updatedAt DESC")
    Page<ItCodeSnippet> searchSnippets(
            @Param("userId") Long userId,
            @Param("language") String language,
            @Param("search") String search,
            Pageable pageable
    );
}
