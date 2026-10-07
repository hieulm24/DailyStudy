package com.englishlearning.repository;

import com.englishlearning.entity.ReviewItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewItemRepository extends JpaRepository<ReviewItem, Long> {

    Optional<ReviewItem> findByUserIdAndContentTypeAndContentId(Long userId, String contentType, Long contentId);

    List<ReviewItem> findByUserId(Long userId);

    @Query("SELECT r FROM ReviewItem r WHERE r.user.id = :userId AND r.reviewStatus = 'ACTIVE' AND (r.nextReviewAt IS NULL OR r.nextReviewAt <= :now) ORDER BY r.nextReviewAt ASC")
    List<ReviewItem> findDueReviewItems(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(r) FROM ReviewItem r WHERE r.user.id = :userId AND r.reviewStatus = 'ACTIVE' AND (r.nextReviewAt IS NULL OR r.nextReviewAt <= :now)")
    long countDueReviewItems(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(r) FROM ReviewItem r WHERE r.user.id = :userId AND r.contentType = :contentType AND r.reviewStatus = 'ACTIVE' AND (r.nextReviewAt IS NULL OR r.nextReviewAt <= :now)")
    long countDueByContentType(@Param("userId") Long userId, @Param("contentType") String contentType, @Param("now") LocalDateTime now);

    @Query("SELECT r FROM ReviewItem r JOIN Vocabulary v ON r.contentType = 'VOCABULARY' AND r.contentId = v.id WHERE r.user.id = :userId AND v.topic.id = :topicId AND r.reviewStatus = 'ACTIVE' AND (r.nextReviewAt IS NULL OR r.nextReviewAt <= :now) ORDER BY r.nextReviewAt ASC")
    List<ReviewItem> findDueReviewItemsByTopic(@Param("userId") Long userId, @Param("topicId") Long topicId, @Param("now") LocalDateTime now);

    @Query("SELECT r FROM ReviewItem r JOIN Vocabulary v ON r.contentType = 'VOCABULARY' AND r.contentId = v.id WHERE r.user.id = :userId AND v.topic.id = :topicId ORDER BY r.nextReviewAt ASC")
    List<ReviewItem> findAllReviewItemsByTopic(@Param("userId") Long userId, @Param("topicId") Long topicId);

    @Query("SELECT COUNT(r) FROM ReviewItem r JOIN Vocabulary v ON r.contentType = 'VOCABULARY' AND r.contentId = v.id WHERE r.user.id = :userId AND v.topic.id = :topicId AND r.reviewStatus = 'ACTIVE' AND (r.nextReviewAt IS NULL OR r.nextReviewAt <= :now)")
    long countDueByTopicId(@Param("userId") Long userId, @Param("topicId") Long topicId, @Param("now") LocalDateTime now);

    void deleteByContentTypeAndContentId(String contentType, Long contentId);
}

