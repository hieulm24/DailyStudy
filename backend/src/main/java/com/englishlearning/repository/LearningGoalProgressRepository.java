package com.englishlearning.repository;

import com.englishlearning.entity.LearningGoalProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningGoalProgressRepository extends JpaRepository<LearningGoalProgress, Long> {
    List<LearningGoalProgress> findByGoalIdOrderByProgressDateDesc(Long goalId);
}
