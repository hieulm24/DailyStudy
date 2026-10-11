package com.englishlearning.repository.financial;

import com.englishlearning.entity.financial.FinancialMonthlyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinancialMonthlyPlanRepository extends JpaRepository<FinancialMonthlyPlan, Long> {
    Optional<FinancialMonthlyPlan> findByMonthAndYear(Integer month, Integer year);
}
