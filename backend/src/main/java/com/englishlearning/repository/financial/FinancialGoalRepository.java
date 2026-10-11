package com.englishlearning.repository.financial;

import com.englishlearning.entity.financial.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {

    List<FinancialGoal> findAllByOrderByCreatedAtDesc();

    List<FinancialGoal> findByStatusOrderByCreatedAtDesc(String status);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
