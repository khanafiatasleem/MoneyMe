package com.moneyme.repository;

import com.moneyme.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByTransactionId(Long transactionId);

    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Query("SELECT COALESCE(SUM(CASE WHEN l.entryType = com.moneyme.entity.EntryType.CREDIT THEN l.amount ELSE -l.amount END), 0) " +
           "FROM LedgerEntry l WHERE l.account.id = :accountId")
    BigDecimal calculateReconciledBalance(@Param("accountId") Long accountId);
}
