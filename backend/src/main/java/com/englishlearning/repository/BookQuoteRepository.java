package com.englishlearning.repository;

import com.englishlearning.entity.BookQuote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookQuoteRepository extends JpaRepository<BookQuote, Long> {

    List<BookQuote> findByBookIdOrderByCreatedAtDesc(Long bookId);

    Optional<BookQuote> findByIdAndBookUserId(Long id, Long userId);

    @Query("SELECT q FROM BookQuote q WHERE q.book.user.id = :userId " +
           "AND (:bookId IS NULL OR q.book.id = :bookId) " +
           "AND (:isFavorite IS NULL OR q.isFavorite = :isFavorite) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(q.quoteText) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(q.note) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY q.createdAt DESC")
    Page<BookQuote> searchQuotes(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId,
            @Param("isFavorite") Boolean isFavorite,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByBookUserId(Long userId);

    long countByBookUserIdAndIsFavoriteTrue(Long userId);
}
