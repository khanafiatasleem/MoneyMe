package com.moneyme.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyme.dto.request.DepositRequest;
import com.moneyme.dto.request.TransferRequest;
import com.moneyme.dto.request.WithdrawRequest;
import com.moneyme.dto.response.AccountResponse;
import com.moneyme.dto.response.DashboardSummaryResponse;
import com.moneyme.dto.response.TransactionResponse;
import com.moneyme.entity.*;
import com.moneyme.exception.BadRequestException;
import com.moneyme.exception.InsufficientBalanceException;
import com.moneyme.exception.ResourceNotFoundException;
import com.moneyme.exception.UnauthorizedAccessException;
import com.moneyme.repository.AccountRepository;
import com.moneyme.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final AuditService auditService;
    private final IdempotencyService idempotencyService;
    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            LedgerService ledgerService,
            AuditService auditService,
            IdempotencyService idempotencyService,
            AccountService accountService,
            ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
        this.idempotencyService = idempotencyService;
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes a deposit into an account owned by the user.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse deposit(Long userId, DepositRequest request, String idempotencyKey) {
        String payloadHash = idempotencyService.computeHash(
                request.getAccountNumber() + ":" + request.getAmount() + ":" + request.getDescription()
        );

        Optional<IdempotencyRecord> cached = idempotencyService.checkAndReserve(idempotencyKey, userId, payloadHash);
        if (cached.isPresent() && cached.get().getResponseBody() != null) {
            try {
                return objectMapper.readValue(cached.get().getResponseBody(), TransactionResponse.class);
            } catch (Exception ignored) {}
        }

        try {
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Deposit amount must be strictly greater than 0.");
            }

            // Lock the account with Pessimistic Write Lock
            Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountNumber()));

            if (!account.getUser().getId().equals(userId)) {
                throw new UnauthorizedAccessException("You can only deposit money into your own accounts.");
            }

            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new BadRequestException("Cannot deposit into account with status: " + account.getStatus());
            }

            // Update balance
            account.setBalance(account.getBalance().add(request.getAmount()));
            accountRepository.save(account);

            String txRef = generateTxRef();
            Transaction transaction = new Transaction(
                    txRef, null, account, TransactionType.DEPOSIT, request.getAmount(),
                    TransactionStatus.SUCCESS,
                    request.getDescription() != null ? request.getDescription() : "Self Deposit",
                    idempotencyKey
            );
            Transaction savedTx = transactionRepository.save(transaction);

            // Record double-entry ledger record
            ledgerService.recordDoubleEntry(savedTx, null, account, request.getAmount());

            // Record immutable audit log
            auditService.log(userId, "DEPOSIT", "TRANSACTION", savedTx.getTransactionRef(),
                    "Deposited " + request.getAmount() + " into " + account.getAccountNumber());

            TransactionResponse response = mapToResponse(savedTx);

            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                try {
                    idempotencyService.recordSuccess(idempotencyKey, objectMapper.writeValueAsString(response), 200);
                } catch (Exception ignored) {}
            }

            return response;
        } catch (Exception e) {
            idempotencyService.releaseKey(idempotencyKey);
            throw e;
        }
    }

    /**
     * Executes a withdrawal from an account owned by the user.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse withdraw(Long userId, WithdrawRequest request, String idempotencyKey) {
        String payloadHash = idempotencyService.computeHash(
                request.getAccountNumber() + ":" + request.getAmount() + ":" + request.getDescription()
        );

        Optional<IdempotencyRecord> cached = idempotencyService.checkAndReserve(idempotencyKey, userId, payloadHash);
        if (cached.isPresent() && cached.get().getResponseBody() != null) {
            try {
                return objectMapper.readValue(cached.get().getResponseBody(), TransactionResponse.class);
            } catch (Exception ignored) {}
        }

        try {
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Withdrawal amount must be strictly greater than 0.");
            }

            Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountNumber()));

            if (!account.getUser().getId().equals(userId)) {
                throw new UnauthorizedAccessException("You can only withdraw money from your own accounts.");
            }

            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new BadRequestException("Cannot withdraw from account with status: " + account.getStatus());
            }

            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException("Insufficient funds. Available balance: " + account.getBalance());
            }

            // Debit account balance
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            accountRepository.save(account);

            String txRef = generateTxRef();
            Transaction transaction = new Transaction(
                    txRef, account, null, TransactionType.WITHDRAWAL, request.getAmount(),
                    TransactionStatus.SUCCESS,
                    request.getDescription() != null ? request.getDescription() : "Withdrawal",
                    idempotencyKey
            );
            Transaction savedTx = transactionRepository.save(transaction);

            // Record double-entry ledger
            ledgerService.recordDoubleEntry(savedTx, account, null, request.getAmount());

            // Audit
            auditService.log(userId, "WITHDRAWAL", "TRANSACTION", savedTx.getTransactionRef(),
                    "Withdrew " + request.getAmount() + " from " + account.getAccountNumber());

            TransactionResponse response = mapToResponse(savedTx);

            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                try {
                    idempotencyService.recordSuccess(idempotencyKey, objectMapper.writeValueAsString(response), 200);
                } catch (Exception ignored) {}
            }

            return response;
        } catch (Exception e) {
            idempotencyService.releaseKey(idempotencyKey);
            throw e;
        }
    }

    /**
     * Executes a money transfer from a sender account to a receiver account.
     * Uses deadlock-free ordered pessimistic locking.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse transfer(Long userId, TransferRequest request, String idempotencyKey) {
        String payloadHash = idempotencyService.computeHash(
                request.getSourceAccountNumber() + ":" + request.getDestinationAccountNumber() + ":"
                        + request.getAmount() + ":" + request.getDescription()
        );

        Optional<IdempotencyRecord> cached = idempotencyService.checkAndReserve(idempotencyKey, userId, payloadHash);
        if (cached.isPresent() && cached.get().getResponseBody() != null) {
            try {
                return objectMapper.readValue(cached.get().getResponseBody(), TransactionResponse.class);
            } catch (Exception ignored) {}
        }

        try {
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Transfer amount must be strictly greater than 0.");
            }

            if (request.getSourceAccountNumber().trim().equalsIgnoreCase(request.getDestinationAccountNumber().trim())) {
                throw new BadRequestException("Source and destination accounts cannot be the same.");
            }

            Account sourceAccountPre = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + request.getSourceAccountNumber()));

            Account destAccountPre = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination account not found: " + request.getDestinationAccountNumber()));

            if (!sourceAccountPre.getUser().getId().equals(userId)) {
                throw new UnauthorizedAccessException("You can only transfer funds from your own account.");
            }

            if (sourceAccountPre.getStatus() != AccountStatus.ACTIVE) {
                throw new BadRequestException("Source account is not active.");
            }

            if (destAccountPre.getStatus() != AccountStatus.ACTIVE) {
                throw new BadRequestException("Destination account is not active.");
            }

            // DEADLOCK PREVENTION: Always lock in ascending order of Account ID!
            Account sourceAccount;
            Account destAccount;

            if (sourceAccountPre.getId() < destAccountPre.getId()) {
                sourceAccount = accountRepository.findByIdForUpdate(sourceAccountPre.getId()).orElseThrow();
                destAccount = accountRepository.findByIdForUpdate(destAccountPre.getId()).orElseThrow();
            } else {
                destAccount = accountRepository.findByIdForUpdate(destAccountPre.getId()).orElseThrow();
                sourceAccount = accountRepository.findByIdForUpdate(sourceAccountPre.getId()).orElseThrow();
            }

            // Check balance under lock
            if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException("Insufficient funds. Available balance: " + sourceAccount.getBalance());
            }

            // Atomic balance updates
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
            destAccount.setBalance(destAccount.getBalance().add(request.getAmount()));

            accountRepository.save(sourceAccount);
            accountRepository.save(destAccount);

            String txRef = generateTxRef();
            Transaction transaction = new Transaction(
                    txRef, sourceAccount, destAccount, TransactionType.TRANSFER, request.getAmount(),
                    TransactionStatus.SUCCESS,
                    request.getDescription() != null ? request.getDescription() : "Account Transfer",
                    idempotencyKey
            );
            Transaction savedTx = transactionRepository.save(transaction);

            // Record double-entry ledger entries (DEBIT source, CREDIT target)
            ledgerService.recordDoubleEntry(savedTx, sourceAccount, destAccount, request.getAmount());

            // Immutable audit log
            auditService.log(userId, "TRANSFER", "TRANSACTION", savedTx.getTransactionRef(),
                    "Transferred " + request.getAmount() + " from " + sourceAccount.getAccountNumber()
                            + " to " + destAccount.getAccountNumber());

            TransactionResponse response = mapToResponse(savedTx);

            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                try {
                    idempotencyService.recordSuccess(idempotencyKey, objectMapper.writeValueAsString(response), 200);
                } catch (Exception ignored) {}
            }

            return response;
        } catch (Exception e) {
            idempotencyService.releaseKey(idempotencyKey);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions(Long userId, TransactionType type) {
        List<Transaction> transactions;
        if (type != null) {
            transactions = transactionRepository.findAllByUserIdAndType(userId, type);
        } else {
            transactions = transactionRepository.findAllByUserId(userId);
        }
        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long userId, Long txId) {
        Transaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + txId));

        boolean isSourceOwner = tx.getSourceAccount() != null && tx.getSourceAccount().getUser().getId().equals(userId);
        boolean isDestOwner = tx.getDestinationAccount() != null && tx.getDestinationAccount().getUser().getId().equals(userId);

        if (!isSourceOwner && !isDestOwner) {
            throw new UnauthorizedAccessException("You are not authorized to view this transaction.");
        }

        return mapToResponse(tx);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(Long userId) {
        List<AccountResponse> accounts = accountService.getUserAccounts(userId);

        BigDecimal totalBalance = accounts.stream()
                .map(AccountResponse::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Transaction> userTxs = transactionRepository.findAllByUserId(userId);

        BigDecimal totalDeposits = userTxs.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT && t.getStatus() == TransactionStatus.SUCCESS)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawals = userTxs.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAWAL && t.getStatus() == TransactionStatus.SUCCESS)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TransactionResponse> recentTransactions = userTxs.stream()
                .limit(10)
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new DashboardSummaryResponse(accounts, totalBalance, totalDeposits, totalWithdrawals, recentTransactions);
    }

    private String generateTxRef() {
        return "TX-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    public TransactionResponse mapToResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getTransactionRef(),
                tx.getSourceAccount() != null ? tx.getSourceAccount().getAccountNumber() : null,
                tx.getDestinationAccount() != null ? tx.getDestinationAccount().getAccountNumber() : null,
                tx.getType().name(),
                tx.getAmount(),
                tx.getStatus().name(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}

