package com.moneyme.controller;

import com.moneyme.config.SecurityUtil;
import com.moneyme.dto.request.DepositRequest;
import com.moneyme.dto.request.TransferRequest;
import com.moneyme.dto.request.WithdrawRequest;
import com.moneyme.dto.response.TransactionResponse;
import com.moneyme.entity.TransactionType;
import com.moneyme.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Long userId = SecurityUtil.getCurrentUserId();
        TransactionResponse response = transactionService.deposit(userId, request, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Long userId = SecurityUtil.getCurrentUserId();
        TransactionResponse response = transactionService.withdraw(userId, request, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Long userId = SecurityUtil.getCurrentUserId();
        TransactionResponse response = transactionService.transfer(userId, request, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(
            @RequestParam(required = false) TransactionType type) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<TransactionResponse> transactions = transactionService.getUserTransactions(userId, type);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        TransactionResponse response = transactionService.getTransactionById(userId, id);
        return ResponseEntity.ok(response);
    }
}

