package com.moneyme.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyme.dto.request.DepositRequest;
import com.moneyme.dto.request.TransferRequest;
import com.moneyme.dto.request.WithdrawRequest;
import com.moneyme.dto.response.TransactionResponse;
import com.moneyme.entity.*;
import com.moneyme.exception.BadRequestException;
import com.moneyme.exception.InsufficientBalanceException;
import com.moneyme.exception.UnauthorizedAccessException;
import com.moneyme.repository.AccountRepository;
import com.moneyme.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private AuditService auditService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private AccountService accountService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User user1;
    private User user2;
    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        user1 = new User("user1@example.com", "pass", "User One", Role.USER);
        user1.setId(1L);

        user2 = new User("user2@example.com", "pass", "User Two", Role.USER);
        user2.setId(2L);

        account1 = new Account("MM11111111", user1, new BigDecimal("500.00"), "USD");
        account1.setId(101L);

        account2 = new Account("MM22222222", user2, new BigDecimal("200.00"), "USD");
        account2.setId(102L);
    }

    @Test
    void deposit_Success() {
        DepositRequest request = new DepositRequest("MM11111111", new BigDecimal("150.00"), "Test Deposit");

        when(idempotencyService.computeHash(anyString())).thenReturn("mockHash");
        when(idempotencyService.checkAndReserve(isNull(), eq(1L), eq("mockHash"))).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("MM11111111")).thenReturn(Optional.of(account1));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.deposit(1L, request, null);

        assertNotNull(response);
        assertEquals(new BigDecimal("150.00"), response.getAmount());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("650.00"), account1.getBalance());
        verify(ledgerService, times(1)).recordDoubleEntry(any(), isNull(), eq(account1), eq(new BigDecimal("150.00")));
    }

    @Test
    void deposit_UnauthorizedAccount_ThrowsUnauthorizedAccessException() {
        DepositRequest request = new DepositRequest("MM11111111", new BigDecimal("100.00"), "Test Deposit");

        when(idempotencyService.computeHash(anyString())).thenReturn("mockHash");
        when(idempotencyService.checkAndReserve(isNull(), eq(2L), eq("mockHash"))).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("MM11111111")).thenReturn(Optional.of(account1));

        // User 2 trying to deposit into User 1's account
        assertThrows(UnauthorizedAccessException.class, () -> transactionService.deposit(2L, request, null));
    }

    @Test
    void deposit_InvalidAmount_ThrowsBadRequestException() {
        DepositRequest request = new DepositRequest("MM11111111", new BigDecimal("-10.00"), "Negative");

        when(idempotencyService.computeHash(anyString())).thenReturn("mockHash");
        when(idempotencyService.checkAndReserve(isNull(), eq(1L), eq("mockHash"))).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> transactionService.deposit(1L, request, null));
    }

    @Test
    void withdraw_Success() {
        WithdrawRequest request = new WithdrawRequest("MM11111111", new BigDecimal("100.00"), "ATM withdrawal");

        when(idempotencyService.computeHash(anyString())).thenReturn("mockHash");
        when(idempotencyService.checkAndReserve(isNull(), eq(1L), eq("mockHash"))).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("MM11111111")).thenReturn(Optional.of(account1));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.withdraw(1L, request, null);

        assertNotNull(response);
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(new BigDecimal("400.00"), account1.getBalance());
        verify(ledgerService, times(1)).recordDoubleEntry(any(), eq(account1), isNull(), eq(new BigDecimal("100.00")));
    }

    @Test
    void withdraw_InsufficientFunds_ThrowsInsufficientBalanceException() {
        WithdrawRequest request = new WithdrawRequest("MM11111111", new BigDecimal("9999.00"), "Overdraft attempt");

        when(idempotencyService.computeHash(anyString())).thenReturn("mockHash");
        when(idempotencyService.checkAndReserve(isNull(), eq(1L), eq("mockHash"))).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("MM11111111")).thenReturn(Optional.of(account1));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.withdraw(1L, request, null));
    }

    @Test
    void transfer_Success() {
        TransferRequest request = new TransferRequest("MM11111111", "MM22222222", new BigDecimal("100.00"), "Payment");

        when(idempotencyService.computeHash(anyString())).thenReturn("mockHash");
        when(idempotencyService.checkAndReserve(isNull(), eq(1L), eq("mockHash"))).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("MM11111111")).thenReturn(Optional.of(account1));
        when(accountRepository.findByAccountNumber("MM22222222")).thenReturn(Optional.of(account2));
        when(accountRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(account1));
        when(accountRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(account2));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.transfer(1L, request, null);

        assertNotNull(response);
        assertEquals(new BigDecimal("400.00"), account1.getBalance());
        assertEquals(new BigDecimal("300.00"), account2.getBalance());
        verify(ledgerService, times(1)).recordDoubleEntry(any(), eq(account1), eq(account2), eq(new BigDecimal("100.00")));
    }

    @Test
    void transfer_SameAccount_ThrowsBadRequestException() {
        TransferRequest request = new TransferRequest("MM11111111", "MM11111111", new BigDecimal("50.00"), "Self transfer");

        when(idempotencyService.computeHash(anyString())).thenReturn("mockHash");
        when(idempotencyService.checkAndReserve(isNull(), eq(1L), eq("mockHash"))).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> transactionService.transfer(1L, request, null));
    }

    @Test
    void transfer_InsufficientBalance_ThrowsInsufficientBalanceException() {
        TransferRequest request = new TransferRequest("MM11111111", "MM22222222", new BigDecimal("1000.00"), "Too high");

        when(idempotencyService.computeHash(anyString())).thenReturn("mockHash");
        when(idempotencyService.checkAndReserve(isNull(), eq(1L), eq("mockHash"))).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("MM11111111")).thenReturn(Optional.of(account1));
        when(accountRepository.findByAccountNumber("MM22222222")).thenReturn(Optional.of(account2));
        when(accountRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(account1));
        when(accountRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(account2));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.transfer(1L, request, null));
    }
}
