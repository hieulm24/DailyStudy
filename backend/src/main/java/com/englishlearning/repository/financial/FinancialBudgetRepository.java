package com.englishlearning.repository.financial;

import com.englishlearning.entity.financial.FinancialBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialBudgetRepository extends JpaRepository<FinancialBudget, Long> {

    List<FinancialBudget> findByMonthAndYear(Integer month, Integer year);

    Optional<FinancialBudget> findByMonthAndYearAndCategoryId(Integer month, Integer year, Long categoryId);

    @Query("SELECT b FROM FinancialBudget b JOIN FETCH b.category c LEFT JOIN FETCH b.account a WHERE b.month = :month AND b.year = :year ORDER BY b.plannedAmount DESC")
    List<FinancialBudget> findByMonthAndYearWithCategory(@Param("month") Integer month, @Param("year") Integer year);

    void deleteByMonthAndYear(Integer month, Integer year);
}
