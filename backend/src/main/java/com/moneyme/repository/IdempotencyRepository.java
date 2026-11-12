package com.moneyme.repository;

import com.moneyme.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {
    Optional<IdempotencyRecord> findByKeyValue(String keyValue);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}

