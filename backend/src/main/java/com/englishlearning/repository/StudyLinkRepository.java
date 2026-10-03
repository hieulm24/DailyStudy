package com.englishlearning.repository;

import com.englishlearning.entity.StudyLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyLinkRepository extends JpaRepository<StudyLink, Long>, JpaSpecificationExecutor<StudyLink> {

    List<StudyLink> findByUserId(Long userId);

    Optional<StudyLink> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCategory(Long userId, String category);

    long countByUserIdAndIsFavoriteTrue(Long userId);
}
