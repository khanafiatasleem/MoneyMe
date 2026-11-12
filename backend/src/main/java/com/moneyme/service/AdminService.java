package com.moneyme.service;

import com.moneyme.dto.response.AccountResponse;
import com.moneyme.dto.response.AdminStatsResponse;
import com.moneyme.dto.response.TransactionResponse;
import com.moneyme.dto.response.UserResponse;
import com.moneyme.entity.Transaction;
import com.moneyme.entity.User;
import com.moneyme.repository.AccountRepository;
import com.moneyme.repository.TransactionRepository;
import com.moneyme.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public AdminService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            AccountService accountService,
            TransactionService transactionService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats() {
        long totalUsers = userRepository.count();
        long totalAccounts = accountRepository.count();
        long totalTransactions = transactionRepository.count();

        List<Transaction> allTransactions = transactionRepository.findAll();
        BigDecimal totalVolume = allTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TransactionResponse> recentTransactions = allTransactions.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(20)
                .map(transactionService::mapToResponse)
                .collect(Collectors.toList());

        List<UserResponse> users = userRepository.findAll().stream()
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());

        return new AdminStatsResponse(totalUsers, totalAccounts, totalTransactions, totalVolume, recentTransactions, users);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(accountService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(transactionService::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse mapUserToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}

