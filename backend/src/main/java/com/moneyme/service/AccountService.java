package com.moneyme.service;

import com.moneyme.dto.request.CreateAccountRequest;
import com.moneyme.dto.response.AccountResponse;
import com.moneyme.entity.*;
import com.moneyme.exception.BadRequestException;
import com.moneyme.exception.ResourceNotFoundException;
import com.moneyme.exception.UnauthorizedAccessException;
import com.moneyme.repository.AccountRepository;
import com.moneyme.repository.TransactionRepository;
import com.moneyme.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            LedgerService ledgerService,
            AuditService auditService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
    }

    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        BigDecimal initialDeposit = request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO;
        if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Initial deposit cannot be negative.");
        }

        String accountNumber = generateUniqueAccountNumber();
        Account account = new Account(accountNumber, user, BigDecimal.ZERO, request.getCurrency());
        Account savedAccount = accountRepository.save(account);

        auditService.log(userId, "ACCOUNT_CREATED", "ACCOUNT", savedAccount.getAccountNumber(),
                "Account created with currency " + savedAccount.getCurrency());

        // Process initial deposit if specified
        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            savedAccount.setBalance(initialDeposit);
            savedAccount = accountRepository.save(savedAccount);

            String txRef = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Transaction transaction = new Transaction(
                    txRef, null, savedAccount, TransactionType.DEPOSIT, initialDeposit,
                    TransactionStatus.SUCCESS, "Initial account opening deposit", null
            );
            Transaction savedTx = transactionRepository.save(transaction);

            ledgerService.recordDoubleEntry(savedTx, null, savedAccount, initialDeposit);
            auditService.log(userId, "INITIAL_DEPOSIT", "TRANSACTION", savedTx.getTransactionRef(),
                    "Amount: " + initialDeposit);
        }

        return mapToResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountDetails(Long userId, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to view this account.");
        }

        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public Account getAccountEntityByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with number: " + accountNumber));
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long number = 10000000L + random.nextInt(90000000);
            accountNumber = "MM" + number;
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    public AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getUser().getId(),
                account.getUser().getFullName(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus().name(),
                account.getCreatedAt()
        );
    }
}

