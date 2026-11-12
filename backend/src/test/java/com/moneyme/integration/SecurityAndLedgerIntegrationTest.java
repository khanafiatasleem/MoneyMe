package com.moneyme.integration;

import com.moneyme.config.JwtTokenProvider;
import com.moneyme.dto.request.CreateAccountRequest;
import com.moneyme.dto.request.RegisterRequest;
import com.moneyme.dto.request.TransferRequest;
import com.moneyme.dto.response.AccountResponse;
import com.moneyme.dto.response.AuthResponse;
import com.moneyme.dto.response.TransactionResponse;
import com.moneyme.entity.EntryType;
import com.moneyme.entity.LedgerEntry;
import com.moneyme.repository.LedgerEntryRepository;
import com.moneyme.service.AccountService;
import com.moneyme.service.AuthService;
import com.moneyme.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityAndLedgerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void testDoubleEntryLedgerBalance_And_SecurityRoles() throws Exception {
        // 1. Create first user (will become ADMIN)
        AuthResponse adminAuth = authService.register(
                new RegisterRequest("System Administrator", "admin@moneyme.com", "AdminPass123!")
        );
        assertEquals("ADMIN", adminAuth.getRole());

        // 2. Create normal USER
        AuthResponse userAuth = authService.register(
                new RegisterRequest("Charlie User", "charlie@moneyme.com", "UserPass123!")
        );
        assertEquals("USER", userAuth.getRole());

        // 3. Create second normal USER
        AuthResponse receiverAuth = authService.register(
                new RegisterRequest("David Receiver", "david@moneyme.com", "ReceiverPass123!")
        );

        // 4. Create accounts with initial balances
        AccountResponse senderAcc = accountService.createAccount(
                userAuth.getUserId(),
                new CreateAccountRequest(new BigDecimal("500.00"), "USD")
        );
        AccountResponse receiverAcc = accountService.createAccount(
                receiverAuth.getUserId(),
                new CreateAccountRequest(new BigDecimal("100.00"), "USD")
        );

        // 5. Execute Money Transfer: $150.00 from Charlie to David
        TransferRequest transferRequest = new TransferRequest(
                senderAcc.getAccountNumber(),
                receiverAcc.getAccountNumber(),
                new BigDecimal("150.00"),
                "Rent Contribution"
        );
        TransactionResponse txResponse = transactionService.transfer(
                userAuth.getUserId(),
                transferRequest,
                "IDEM-TEST-KEY-001"
        );
        assertEquals("SUCCESS", txResponse.getStatus());

        // 6. Verify Double-Entry Ledger Invariant: Total Debits == Total Credits
        List<LedgerEntry> entries = ledgerEntryRepository.findByTransactionId(txResponse.getId());
        assertEquals(2, entries.size(), "A completed transfer must produce exactly 2 ledger entries");

        BigDecimal totalDebits = entries.stream()
                .filter(e -> e.getEntryType() == EntryType.DEBIT)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = entries.stream()
                .filter(e -> e.getEntryType() == EntryType.CREDIT)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("150.0000"), totalDebits);
        assertEquals(new BigDecimal("150.0000"), totalCredits);
        assertEquals(totalDebits, totalCredits, "Double entry balance invariant violated: Sum(Debits) != Sum(Credits)");

        // 7. Security: Verify USER cannot access ADMIN endpoint
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + userAuth.getToken()))
                .andExpect(status().isForbidden());

        // 8. Security: Verify ADMIN CAN access ADMIN endpoint
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminAuth.getToken()))
                .andExpect(status().isOk());
    }
}

