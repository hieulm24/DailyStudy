package com.englishlearning.repository.financial;

import com.englishlearning.entity.financial.FinancialGoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinancialGoalContributionRepository extends JpaRepository<FinancialGoalContribution, Long> {

    List<FinancialGoalContribution> findByGoalIdOrderByContributionDateDesc(Long goalId);

    List<FinancialGoalContribution> findByAccountIdOrderByContributionDateDesc(Long accountId);

    @Query("SELECT COALESCE(SUM(CASE WHEN c.type = 'DEPOSIT' THEN c.amount ELSE -c.amount END), 0) FROM FinancialGoalContribution c WHERE c.goal.id = :goalId")
    BigDecimal sumNetContributionsForGoal(@Param("goalId") Long goalId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM FinancialGoalContribution c WHERE c.goal.id = :goalId AND c.type = 'DEPOSIT'")
    BigDecimal sumDepositsForGoal(@Param("goalId") Long goalId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM FinancialGoalContribution c WHERE c.goal.id = :goalId AND c.type = 'WITHDRAW'")
    BigDecimal sumWithdrawalsForGoal(@Param("goalId") Long goalId);

    @Query("SELECT COALESCE(SUM(CASE WHEN c.type = 'DEPOSIT' THEN c.amount ELSE -c.amount END), 0) FROM FinancialGoalContribution c WHERE c.account.id = :accountId")
    BigDecimal sumNetContributionsForAccount(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(CASE WHEN c.type = 'DEPOSIT' THEN c.amount ELSE -c.amount END), 0) FROM FinancialGoalContribution c WHERE c.goal.status = 'ACTIVE'")
    BigDecimal sumTotalAllocatedActiveGoals();

    @Query("SELECT COALESCE(SUM(CASE WHEN c.type = 'DEPOSIT' THEN c.amount ELSE -c.amount END), 0) FROM FinancialGoalContribution c WHERE c.goal.id = :goalId AND c.contributionDate >= :sinceDate")
    BigDecimal sumNetContributionsSince(@Param("goalId") Long goalId, @Param("sinceDate") LocalDateTime sinceDate);

    boolean existsByGoalId(Long goalId);

    boolean existsByAccountId(Long accountId);
}
