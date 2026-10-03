package com.englishlearning.repository;

import com.englishlearning.entity.GrammarTopic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrammarTopicRepository extends JpaRepository<GrammarTopic, Long>, JpaSpecificationExecutor<GrammarTopic> {

    List<GrammarTopic> findByUserId(Long userId);

    Optional<GrammarTopic> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT g FROM GrammarTopic g WHERE g.user.id = :userId ORDER BY function('NEWID')")
    List<GrammarTopic> findRandomGrammars(@Param("userId") Long userId, Pageable pageable);
}
