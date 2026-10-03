package com.englishlearning.repository;

import com.englishlearning.entity.VocabularyExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyExampleRepository extends JpaRepository<VocabularyExample, Long> {
    List<VocabularyExample> findByVocabularyId(Long vocabularyId);
    void deleteByVocabularyId(Long vocabularyId);
}
