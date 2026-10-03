package com.englishlearning.repository;

import com.englishlearning.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    List<Document> findByUserId(Long userId);

    Optional<Document> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndFileType(Long userId, String fileType);

    long countByUserIdAndCategory(Long userId, String category);

    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM Document d WHERE d.user.id = :userId")
    Long sumFileSizeByUserId(@Param("userId") Long userId);
}
