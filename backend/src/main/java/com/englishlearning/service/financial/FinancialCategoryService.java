package com.englishlearning.service.financial;

import com.englishlearning.common.BadRequestException;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.financial.CategoryDtos.*;
import com.englishlearning.entity.financial.FinancialCategory;
import com.englishlearning.repository.financial.FinancialCategoryRepository;
import com.englishlearning.repository.financial.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialCategoryService {

    private final FinancialCategoryRepository categoryRepository;
    private final FinancialTransactionRepository transactionRepository;

    @Transactional
    public List<CategoryResponse> getAllCategories(String type, Boolean activeOnly) {
        // Seed default categories if DB is empty
        if (categoryRepository.count() == 0) {
            seedDefaultCategories();
        }

        List<FinancialCategory> categories;
        if (type != null && !type.isBlank()) {
            categories = categoryRepository.findByTypeAndIsActiveTrueOrderByNameAsc(type.toUpperCase());
        } else if (Boolean.TRUE.equals(activeOnly)) {
            categories = categoryRepository.findByIsActiveTrueOrderByTypeAscNameAsc();
        } else {
            categories = categoryRepository.findAllByOrderByTypeAscNameAsc();
        }

        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        FinancialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String name = request.getName().trim();
        String type = request.getType().toUpperCase();

        if (categoryRepository.existsByNameIgnoreCaseAndType(name, type)) {
            throw new BadRequestException("Danh mục '" + name + "' trong nhóm " + type + " đã tồn tại");
        }

        FinancialCategory category = FinancialCategory.builder()
                .name(name)
                .type(type)
                .icon(request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon() : "Folder")
                .color(request.getColor() != null && !request.getColor().isBlank() ? request.getColor() : "#64748b")
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        FinancialCategory saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        FinancialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        String name = request.getName().trim();
        String type = request.getType().toUpperCase();

        if (categoryRepository.existsByNameIgnoreCaseAndTypeAndIdNot(name, type, id)) {
            throw new BadRequestException("Danh mục '" + name + "' trong nhóm " + type + " đã tồn tại");
        }

        category.setName(name);
        category.setType(type);
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getColor() != null) category.setColor(request.getColor());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());

        FinancialCategory updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Transactional
    public CategoryResponse toggleCategoryStatus(Long id) {
        FinancialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        category.setIsActive(!Boolean.TRUE.equals(category.getIsActive()));
        FinancialCategory updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        FinancialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        if (transactionRepository.existsByCategoryId(id)) {
            throw new BadRequestException("Không thể xóa danh mục đã được dùng trong các giao dịch. Vui lòng chọn 'Ngừng sử dụng'!");
        }

        categoryRepository.delete(category);
    }

    @Transactional
    public void seedDefaultCategories() {
        if (categoryRepository.count() > 0) return;

        List<FinancialCategory> defaults = new ArrayList<>();

        // Thu nhập (INCOME)
        defaults.add(FinancialCategory.builder().name("Lương chính").type("INCOME").icon("Briefcase").color("#10b981").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Thưởng & Hoa hồng").type("INCOME").icon("Gift").color("#f59e0b").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Thu nhập phụ / Freelance").type("INCOME").icon("Laptop").color("#6366f1").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Đầu tư & Lãi tiết kiệm").type("INCOME").icon("TrendingUp").color("#06b6d4").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Thu nhập khác").type("INCOME").icon("PlusCircle").color("#84cc16").isDefault(true).isActive(true).build());

        // Chi tiêu (EXPENSE)
        defaults.add(FinancialCategory.builder().name("Ăn uống & Cà phê").type("EXPENSE").icon("Utensils").color("#ef4444").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Tiền nhà & Điện nước").type("EXPENSE").icon("Home").color("#8b5cf6").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Đi lại & Xăng xe").type("EXPENSE").icon("Car").color("#f97316").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Mua sắm & Đồ gia dụng").type("EXPENSE").icon("ShoppingBag").color("#ec4899").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Sức khỏe & Thể thao").type("EXPENSE").icon("HeartPulse").color("#14b8a6").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Học tập & Sách vở").type("EXPENSE").icon("GraduationCap").color("#3b82f6").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Gia đình & Hiếu hỷ").type("EXPENSE").icon("Users").color("#0ea5e9").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Giải trí & Du lịch").type("EXPENSE").icon("Gamepad2").color("#a855f7").isDefault(true).isActive(true).build());
        defaults.add(FinancialCategory.builder().name("Chi phí khác").type("EXPENSE").icon("MoreHorizontal").color("#64748b").isDefault(true).isActive(true).build());

        categoryRepository.saveAll(defaults);
    }

    public CategoryResponse mapToResponse(FinancialCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .color(category.getColor())
                .isDefault(category.getIsDefault())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
