package com.englishlearning.service.financial;

import com.englishlearning.common.BadRequestException;
import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.financial.TransactionDtos.*;
import com.englishlearning.entity.financial.FinancialAccount;
import com.englishlearning.entity.financial.FinancialCategory;
import com.englishlearning.entity.financial.FinancialTransaction;
import com.englishlearning.repository.financial.FinancialAccountRepository;
import com.englishlearning.repository.financial.FinancialCategoryRepository;
import com.englishlearning.repository.financial.FinancialTransactionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialTransactionService {

    private final FinancialTransactionRepository transactionRepository;
    private final FinancialAccountRepository accountRepository;
    private final FinancialCategoryRepository categoryRepository;
    private final FinancialAccountService accountService;

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactions(TransactionFilterRequest filter) {
        Specification<FinancialTransaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getType() != null && !filter.getType().isBlank()) {
                predicates.add(cb.equal(root.get("type"), filter.getType().toUpperCase()));
            }

            if (filter.getAccountId() != null) {
                Predicate fromAccount = cb.equal(root.get("account").get("id"), filter.getAccountId());
                Predicate toAccount = cb.equal(root.get("toAccount").get("id"), filter.getAccountId());
                predicates.add(cb.or(fromAccount, toAccount));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.getEndDate()));
            }

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";
                Predicate matchDesc = cb.like(cb.lower(root.get("description")), keyword);
                Predicate matchNote = cb.like(cb.lower(root.get("note")), keyword);
                predicates.add(cb.or(matchDesc, matchNote));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = "transactionDate".equalsIgnoreCase(filter.getSortBy()) ? "transactionDate"
                : ("id".equalsIgnoreCase(filter.getSortBy()) ? "id" : "createdAt");
        Sort sort = "id".equalsIgnoreCase(sortField)
                ? Sort.by(direction, "id")
                : Sort.by(direction, sortField).and(Sort.by(Sort.Direction.DESC, "id"));

        Pageable pageable = PageRequest.of(Math.max(0, filter.getPage()), Math.max(1, filter.getSize()), sort);
        Page<FinancialTransaction> page = transactionRepository.findAll(spec, pageable);

        List<TransactionResponse> items = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<TransactionResponse>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        FinancialTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch với ID: " + id));
        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        validateTransactionRequest(request);

        FinancialAccount account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + request.getAccountId()));

        FinancialCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));
        }

        FinancialAccount toAccount = null;
        String type = request.getType().toUpperCase();
        if ("TRANSFER".equalsIgnoreCase(type)) {
            toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản nhận với ID: " + request.getToAccountId()));
        }

        // Kiểm tra số dư ví chi tiền / chuyển đi (chặn số dư âm đối với các ví thông thường)
        if ("EXPENSE".equals(type) || "TRANSFER".equals(type)) {
            if (!"CREDIT".equalsIgnoreCase(account.getAccountType())) {
                BigDecimal currentBal = accountService.mapToResponse(account).getCurrentBalance();
                if (currentBal == null) currentBal = BigDecimal.ZERO;
                if (request.getAmount().compareTo(currentBal) > 0) {
                    String action = "TRANSFER".equals(type) ? "chuyển tiền" : "chi tiêu";
                    throw new BadRequestException(String.format(
                            "Số dư ví '%s' không đủ để %s! (Hiện có: %s, Cần: %s)",
                            account.getName(), action, formatVnMoney(currentBal), formatVnMoney(request.getAmount())));
                }
            }
        }

        FinancialTransaction transaction = FinancialTransaction.builder()
                .account(account)
                .category(category)
                .type(type)
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now())
                .description(request.getDescription())
                .note(request.getNote())
                .toAccount(toAccount)
                .build();

        FinancialTransaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        validateTransactionRequest(request);

        FinancialTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch với ID: " + id));

        FinancialAccount account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + request.getAccountId()));

        FinancialCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));
        }

        FinancialAccount toAccount = null;
        String type = request.getType().toUpperCase();
        if ("TRANSFER".equalsIgnoreCase(type)) {
            toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản nhận với ID: " + request.getToAccountId()));
        }

        // Kiểm tra số dư ví khi cập nhật giao dịch
        if ("EXPENSE".equals(type) || "TRANSFER".equals(type)) {
            if (!"CREDIT".equalsIgnoreCase(account.getAccountType())) {
                BigDecimal currentBal = accountService.mapToResponse(account).getCurrentBalance();
                if (currentBal == null) currentBal = BigDecimal.ZERO;

                // Nếu giao dịch cũ cũng thuộc tài khoản này và cùng là loại trừ tiền thì hoàn lại số tiền cũ trước khi so sánh
                BigDecimal effectiveBal = currentBal;
                if (transaction.getAccount() != null && transaction.getAccount().getId().equals(account.getId())
                        && ("EXPENSE".equalsIgnoreCase(transaction.getType()) || "TRANSFER".equalsIgnoreCase(transaction.getType()))) {
                    effectiveBal = effectiveBal.add(transaction.getAmount());
                }

                if (request.getAmount().compareTo(effectiveBal) > 0) {
                    String action = "TRANSFER".equals(type) ? "chuyển tiền" : "chi tiêu";
                    throw new BadRequestException(String.format(
                            "Số dư ví '%s' không đủ để %s! (Hiện có: %s, Cần: %s)",
                            account.getName(), action, formatVnMoney(effectiveBal), formatVnMoney(request.getAmount())));
                }
            }
        }

        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setType(type);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now());
        transaction.setDescription(request.getDescription());
        transaction.setNote(request.getNote());
        transaction.setToAccount(toAccount);

        FinancialTransaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        FinancialTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch với ID: " + id));

        transactionRepository.delete(transaction);
    }

    private void validateTransactionRequest(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Số tiền giao dịch phải lớn hơn 0");
        }

        String type = request.getType() != null ? request.getType().toUpperCase() : "";
        if (!type.equals("INCOME") && !type.equals("EXPENSE") && !type.equals("TRANSFER")) {
            throw new BadRequestException("Loại giao dịch không hợp lệ: " + type + ". Chỉ chấp nhận INCOME, EXPENSE hoặc TRANSFER");
        }

        if (type.equals("TRANSFER")) {
            if (request.getToAccountId() == null) {
                throw new BadRequestException("Giao dịch chuyển tiền yêu cầu chọn tài khoản đích");
            }
            if (request.getToAccountId().equals(request.getAccountId())) {
                throw new BadRequestException("Tài khoản nhận tiền phải khác tài khoản chuyển đi");
            }
        }
    }

    public TransactionResponse mapToResponse(FinancialTransaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .accountId(t.getAccount() != null ? t.getAccount().getId() : null)
                .accountName(t.getAccount() != null ? t.getAccount().getName() : null)
                .accountType(t.getAccount() != null ? t.getAccount().getAccountType() : null)
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                .categoryIcon(t.getCategory() != null ? t.getCategory().getIcon() : null)
                .categoryColor(t.getCategory() != null ? t.getCategory().getColor() : null)
                .type(t.getType())
                .amount(t.getAmount())
                .transactionDate(t.getTransactionDate())
                .description(t.getDescription())
                .note(t.getNote())
                .toAccountId(t.getToAccount() != null ? t.getToAccount().getId() : null)
                .toAccountName(t.getToAccount() != null ? t.getToAccount().getName() : null)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private String formatVnMoney(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount) + " ₫";
    }
}
