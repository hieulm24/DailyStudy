package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.financial.AccountDtos.*;
import com.englishlearning.service.financial.FinancialAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financial/accounts")
@RequiredArgsConstructor
public class FinancialAccountController {

    private final FinancialAccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts(
            @RequestParam(value = "activeOnly", required = false, defaultValue = "false") Boolean activeOnly) {
        List<AccountResponse> response = accountService.getAllAccounts(activeOnly);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable Long id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.ok(ApiResponse.ok("Thêm tài khoản thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.updateAccount(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin tài khoản thành công", response));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<AccountResponse>> toggleAccountStatus(@PathVariable Long id) {
        AccountResponse response = accountService.toggleAccountStatus(id);
        return ResponseEntity.ok(ApiResponse.ok("Thay đổi trạng thái tài khoản thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa tài khoản thành công", null));
    }
}
