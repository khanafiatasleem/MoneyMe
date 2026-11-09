package com.moneyme.service;

import com.moneyme.dto.request.CreateAccountRequest;
import com.moneyme.dto.response.AccountResponse;
import com.moneyme.entity.Account;
import com.moneyme.entity.Role;
import com.moneyme.entity.User;
import com.moneyme.exception.BadRequestException;
import com.moneyme.exception.ResourceNotFoundException;
import com.moneyme.exception.UnauthorizedAccessException;
import com.moneyme.repository.AccountRepository;
import com.moneyme.repository.TransactionRepository;
import com.moneyme.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User("user@example.com", "hash", "Bob Test", Role.USER);
        user.setId(10L);

        account = new Account("MM12345678", user, new BigDecimal("100.00"), "USD");
        account.setId(100L);
    }

    @Test
    void createAccount_Success() {
        CreateAccountRequest request = new CreateAccountRequest(BigDecimal.ZERO, "USD");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountService.createAccount(10L, request);

        assertNotNull(response);
        assertEquals("MM12345678", response.getAccountNumber());
        assertEquals(10L, response.getUserId());
    }

    @Test
    void createAccount_NegativeDeposit_ThrowsBadRequestException() {
        CreateAccountRequest request = new CreateAccountRequest(new BigDecimal("-50.00"), "USD");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> accountService.createAccount(10L, request));
    }

    @Test
    void getAccountDetails_UnauthorizedUser_ThrowsUnauthorizedAccessException() {
        when(accountRepository.findById(100L)).thenReturn(Optional.of(account));

        // User ID 99 attempts to access User ID 10's account
        assertThrows(UnauthorizedAccessException.class, () -> accountService.getAccountDetails(99L, 100L));
    }

    @Test
    void getAccountDetails_NonExistent_ThrowsResourceNotFoundException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountDetails(10L, 999L));
    }
}
