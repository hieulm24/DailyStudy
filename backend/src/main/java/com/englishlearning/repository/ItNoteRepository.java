package com.englishlearning.repository;

import com.englishlearning.entity.ItNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItNoteRepository extends JpaRepository<ItNote, Long>, JpaSpecificationExecutor<ItNote> {

    List<ItNote> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<ItNote> findByIdAndUserId(Long id, Long userId);

    Page<ItNote> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT n FROM ItNote n WHERE n.user.id = :userId AND " +
           "(:category IS NULL OR :category = '' OR n.category = :category) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.contentMarkdown) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.tags) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY n.isFavorite DESC, n.updatedAt DESC")
    Page<ItNote> searchNotes(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("search") String search,
            Pageable pageable
    );

    long countByUserId(Long userId);
}
