package com.moneyme.repository;

import com.moneyme.entity.Transaction;
import com.moneyme.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionRef(String transactionRef);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN t.sourceAccount sa " +
           "LEFT JOIN t.destinationAccount da " +
           "WHERE (sa.user.id = :userId OR da.user.id = :userId) " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN t.sourceAccount sa " +
           "LEFT JOIN t.destinationAccount da " +
           "WHERE (sa.user.id = :userId OR da.user.id = :userId) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findAllByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN t.sourceAccount sa " +
           "LEFT JOIN t.destinationAccount da " +
           "WHERE (sa.id = :accountId OR da.id = :accountId) " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findAllByAccountId(@Param("accountId") Long accountId);
}
