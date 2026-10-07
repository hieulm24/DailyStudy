package com.englishlearning.repository;

import com.englishlearning.entity.HealthWorkoutPhoto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthWorkoutPhotoRepository extends JpaRepository<HealthWorkoutPhoto, Long> {

    List<HealthWorkoutPhoto> findByUserIdAndLogDateOrderByCreatedAtDesc(Long userId, LocalDate logDate);

    Page<HealthWorkoutPhoto> findByUserIdOrderByLogDateDescCreatedAtDesc(Long userId, Pageable pageable);

    Optional<HealthWorkoutPhoto> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
