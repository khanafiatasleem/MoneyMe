package com.moneyme.controller;

import com.moneyme.config.SecurityUtil;
import com.moneyme.dto.request.CreateAccountRequest;
import com.moneyme.dto.response.AccountResponse;
import com.moneyme.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody(required = false) CreateAccountRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (request == null) {
            request = new CreateAccountRequest();
        }
        AccountResponse response = accountService.createAccount(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<AccountResponse> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountDetails(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        AccountResponse account = accountService.getAccountDetails(userId, id);
        return ResponseEntity.ok(account);
    }
}
