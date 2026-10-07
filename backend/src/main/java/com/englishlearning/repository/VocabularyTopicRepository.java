package com.englishlearning.repository;

import com.englishlearning.entity.VocabularyTopic;
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
public interface VocabularyTopicRepository extends JpaRepository<VocabularyTopic, Long>, JpaSpecificationExecutor<VocabularyTopic> {

    List<VocabularyTopic> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<VocabularyTopic> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    @Query("SELECT COUNT(v) FROM Vocabulary v WHERE v.topic.id = :topicId")
    long countVocabulariesByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT COUNT(v) FROM Vocabulary v WHERE v.topic.id = :topicId AND v.status = :status")
    long countVocabulariesByTopicIdAndStatus(@Param("topicId") Long topicId, @Param("status") String status);
}
