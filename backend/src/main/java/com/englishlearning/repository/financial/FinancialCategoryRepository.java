package com.englishlearning.repository.financial;

import com.englishlearning.entity.financial.FinancialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialCategoryRepository extends JpaRepository<FinancialCategory, Long> {

    List<FinancialCategory> findAllByOrderByTypeAscNameAsc();

    List<FinancialCategory> findByIsActiveTrueOrderByTypeAscNameAsc();

    List<FinancialCategory> findByTypeAndIsActiveTrueOrderByNameAsc(String type);

    boolean existsByNameIgnoreCaseAndType(String name, String type);

    boolean existsByNameIgnoreCaseAndTypeAndIdNot(String name, String type, Long id);
}
