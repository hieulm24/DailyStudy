package com.englishlearning.service.financial;

import com.englishlearning.common.BadRequestException;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.financial.AccountDtos.*;
import com.englishlearning.entity.financial.FinancialAccount;
import com.englishlearning.repository.financial.FinancialAccountRepository;
import com.englishlearning.repository.financial.FinancialGoalContributionRepository;
import com.englishlearning.repository.financial.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialAccountService {

    private final FinancialAccountRepository accountRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final FinancialGoalContributionRepository goalContributionRepository;

    @Transactional
    public List<AccountResponse> getAllAccounts(Boolean activeOnly) {
        ensureDefaultCashAccount();
        List<FinancialAccount> accounts = Boolean.TRUE.equals(activeOnly)
                ? accountRepository.findByIsActiveTrueOrderByNameAsc()
                : accountRepository.findAllByOrderByCreatedAtDesc();

        return accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void ensureDefaultCashAccount() {
        if (!accountRepository.existsByAccountType("CASH") && !accountRepository.existsByNameIgnoreCase("Tiền mặt")) {
            FinancialAccount cash = FinancialAccount.builder()
                    .name("Tiền mặt")
                    .accountType("CASH")
                    .initialBalance(BigDecimal.ZERO)
                    .isActive(true)
                    .build();
            accountRepository.save(cash);
        }
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        FinancialAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        if (accountRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Tài khoản có tên '" + request.getName().trim() + "' đã tồn tại");
        }

        if (request.getInitialBalance() != null && request.getInitialBalance().compareTo(BigDecimal.ZERO) < 0
                && !"CREDIT".equalsIgnoreCase(request.getAccountType())) {
            throw new BadRequestException("Số dư ban đầu không được âm đối với loại tài khoản này");
        }

        FinancialAccount account = FinancialAccount.builder()
                .name(request.getName().trim())
                .accountType(request.getAccountType().toUpperCase())
                .initialBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        FinancialAccount saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    @Transactional
    public AccountResponse updateAccount(Long id, AccountRequest request) {
        FinancialAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));

        if (accountRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new BadRequestException("Tài khoản có tên '" + request.getName().trim() + "' đã tồn tại");
        }

        if (request.getInitialBalance() != null && request.getInitialBalance().compareTo(BigDecimal.ZERO) < 0
                && !"CREDIT".equalsIgnoreCase(request.getAccountType())) {
            throw new BadRequestException("Số dư ban đầu không được âm đối với loại tài khoản này");
        }

        account.setName(request.getName().trim());
        account.setAccountType(request.getAccountType().toUpperCase());
        if (request.getInitialBalance() != null) {
            account.setInitialBalance(request.getInitialBalance());
        }
        if (request.getIsActive() != null) {
            account.setIsActive(request.getIsActive());
        }

        FinancialAccount updated = accountRepository.save(account);
        return mapToResponse(updated);
    }

    @Transactional
    public AccountResponse toggleAccountStatus(Long id) {
        FinancialAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));

        account.setIsActive(!Boolean.TRUE.equals(account.getIsActive()));
        FinancialAccount updated = accountRepository.save(account);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAccount(Long id) {
        FinancialAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));

        boolean hasTransactions = transactionRepository.existsByAccountIdOrToAccountId(id, id);
        if (hasTransactions) {
            throw new BadRequestException("Không thể xóa tài khoản đã có phát sinh giao dịch. Vui lòng chuyển trạng thái sang 'Ngừng sử dụng'!");
        }

        boolean hasGoalContributions = goalContributionRepository.existsByAccountId(id);
        if (hasGoalContributions) {
            throw new BadRequestException("Không thể xóa tài khoản đã có đóng góp cho mục tiêu tài chính. Vui lòng chọn 'Ngừng sử dụng'!");
        }

        accountRepository.delete(account);
    }

    public AccountResponse mapToResponse(FinancialAccount account) {
        BigDecimal initial = account.getInitialBalance() != null ? account.getInitialBalance() : BigDecimal.ZERO;
        Long id = account.getId();

        BigDecimal income = transactionRepository.sumIncomeForAccount(id);
        BigDecimal expense = transactionRepository.sumExpenseForAccount(id);
        BigDecimal inTransfer = transactionRepository.sumIncomingTransferForAccount(id);
        BigDecimal outTransfer = transactionRepository.sumOutgoingTransferForAccount(id);

        BigDecimal currentBalance = initial
                .add(income != null ? income : BigDecimal.ZERO)
                .subtract(expense != null ? expense : BigDecimal.ZERO)
                .add(inTransfer != null ? inTransfer : BigDecimal.ZERO)
                .subtract(outTransfer != null ? outTransfer : BigDecimal.ZERO);

        BigDecimal allocated = goalContributionRepository.sumNetContributionsForAccount(id);
        if (allocated == null || allocated.compareTo(BigDecimal.ZERO) < 0) {
            allocated = BigDecimal.ZERO;
        }

        BigDecimal availableBalance = currentBalance.subtract(allocated);

        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .accountType(account.getAccountType())
                .initialBalance(initial)
                .isActive(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .currentBalance(currentBalance)
                .allocatedToGoal(allocated)
                .availableBalance(availableBalance)
                .build();
    }
}
