package com.englishlearning.repository.financial;

import com.englishlearning.entity.financial.FinancialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, Long> {

    List<FinancialAccount> findAllByOrderByCreatedAtDesc();

    List<FinancialAccount> findByIsActiveTrueOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
