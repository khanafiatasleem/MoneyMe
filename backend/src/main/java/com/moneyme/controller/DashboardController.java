package com.moneyme.controller;

import com.moneyme.config.SecurityUtil;
import com.moneyme.dto.response.DashboardSummaryResponse;
import com.moneyme.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TransactionService transactionService;

    public DashboardController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<DashboardSummaryResponse> getDashboard() {
        Long userId = SecurityUtil.getCurrentUserId();
        DashboardSummaryResponse summary = transactionService.getDashboardSummary(userId);
        return ResponseEntity.ok(summary);
    }
}
