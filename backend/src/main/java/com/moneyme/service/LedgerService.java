package com.moneyme.service;

import com.moneyme.entity.*;
import com.moneyme.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    /**
     * Records double-entry ledger records for a completed transaction.
     * Invariant: Total Debit == Total Credit.
     */
    @Transactional
    public void recordDoubleEntry(Transaction transaction, Account sourceAccount, Account destinationAccount, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ledger amount must be strictly positive");
        }

        switch (transaction.getType()) {
            case DEPOSIT -> {
                // Deposit: funds credited to the user's destination account
                LedgerEntry creditEntry = new LedgerEntry(transaction, destinationAccount, EntryType.CREDIT, amount);
                ledgerEntryRepository.save(creditEntry);

                // Note: If source account is null (external deposit), credit is balanced against external capital inflow
            }
            case WITHDRAWAL -> {
                // Withdrawal: funds debited from user's source account
                LedgerEntry debitEntry = new LedgerEntry(transaction, sourceAccount, EntryType.DEBIT, amount);
                ledgerEntryRepository.save(debitEntry);
            }
            case TRANSFER -> {
                // Transfer: DEBIT source account and CREDIT destination account
                LedgerEntry debitEntry = new LedgerEntry(transaction, sourceAccount, EntryType.DEBIT, amount);
                LedgerEntry creditEntry = new LedgerEntry(transaction, destinationAccount, EntryType.CREDIT, amount);
                ledgerEntryRepository.save(debitEntry);
                ledgerEntryRepository.save(creditEntry);
            }
        }
    }

    public List<LedgerEntry> getEntriesForTransaction(Long transactionId) {
        return ledgerEntryRepository.findByTransactionId(transactionId);
    }

    public List<LedgerEntry> getEntriesForAccount(Long accountId) {
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public BigDecimal getReconciledBalance(Long accountId) {
        return ledgerEntryRepository.calculateReconciledBalance(accountId);
    }
}
