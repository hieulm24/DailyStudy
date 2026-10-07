package com.englishlearning.repository;

import com.englishlearning.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT b FROM Book b WHERE b.user.id = :userId " +
           "AND (:status IS NULL OR :status = '' OR b.status = :status) " +
           "AND (:category IS NULL OR :category = '' OR b.category = :category) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY b.updatedAt DESC")
    Page<Book> searchBooks(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Book> findByUserIdOrderByUpdatedAtDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.user.id = :userId AND b.category IS NOT NULL AND b.category <> '' ORDER BY b.category")
    List<String> findDistinctCategoriesByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(b.currentPage) FROM Book b WHERE b.user.id = :userId")
    Long sumTotalPagesReadByUserId(@Param("userId") Long userId);
}
