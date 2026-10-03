package com.englishlearning.repository;

import com.englishlearning.entity.GrammarExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrammarExampleRepository extends JpaRepository<GrammarExample, Long> {
    List<GrammarExample> findByGrammarTopicId(Long grammarTopicId);
    void deleteByGrammarTopicId(Long grammarTopicId);
}
