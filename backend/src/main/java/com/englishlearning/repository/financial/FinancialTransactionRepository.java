package com.englishlearning.repository.financial;

import com.englishlearning.entity.financial.FinancialTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long>, JpaSpecificationExecutor<FinancialTransaction> {

    List<FinancialTransaction> findTop10ByOrderByTransactionDateDescCreatedAtDesc();

    Page<FinancialTransaction> findByAccountIdOrToAccountIdOrderByTransactionDateDesc(Long accountId, Long toAccountId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinancialTransaction t WHERE t.type = 'INCOME' AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumIncomeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinancialTransaction t WHERE t.type = 'EXPENSE' AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumExpenseBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinancialTransaction t WHERE t.account.id = :accountId AND t.type = 'INCOME'")
    BigDecimal sumIncomeForAccount(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinancialTransaction t WHERE t.account.id = :accountId AND t.type = 'EXPENSE'")
    BigDecimal sumExpenseForAccount(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinancialTransaction t WHERE t.account.id = :accountId AND t.type = 'TRANSFER'")
    BigDecimal sumOutgoingTransferForAccount(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinancialTransaction t WHERE t.toAccount.id = :accountId AND t.type = 'TRANSFER'")
    BigDecimal sumIncomingTransferForAccount(@Param("accountId") Long accountId);

    @Query("SELECT t.category.id, t.category.name, t.category.icon, t.category.color, COALESCE(SUM(t.amount), 0), COUNT(t.id) " +
           "FROM FinancialTransaction t " +
           "WHERE t.type = 'EXPENSE' AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
           "GROUP BY t.category.id, t.category.name, t.category.icon, t.category.color " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> getCategoryExpenseBreakdown(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t.category.id, t.category.name, t.category.icon, t.category.color, COALESCE(SUM(t.amount), 0), COUNT(t.id) " +
           "FROM FinancialTransaction t " +
           "WHERE t.type = 'INCOME' AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
           "GROUP BY t.category.id, t.category.name, t.category.icon, t.category.color " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> getCategoryIncomeBreakdown(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    boolean existsByAccountIdOrToAccountId(Long accountId, Long toAccountId);

    boolean existsByCategoryId(Long categoryId);
}
