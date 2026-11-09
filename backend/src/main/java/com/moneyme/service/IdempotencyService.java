package com.moneyme.service;

import com.moneyme.entity.IdempotencyRecord;
import com.moneyme.exception.BadRequestException;
import com.moneyme.exception.DuplicateIdempotencyException;
import com.moneyme.repository.IdempotencyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final long ttlMinutes;

    public IdempotencyService(
            IdempotencyRepository idempotencyRepository,
            @Value("${app.idempotency.ttl-minutes:1440}") long ttlMinutes) {
        this.idempotencyRepository = idempotencyRepository;
        this.ttlMinutes = ttlMinutes;
    }

    public String computeHash(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((payload != null ? payload : "").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Transactional
    public Optional<IdempotencyRecord> checkAndReserve(String key, Long userId, String requestHash) {
        if (key == null || key.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<IdempotencyRecord> existing = idempotencyRepository.findByKeyValue(key);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw new BadRequestException("Idempotency key reused with a different request payload.");
            }
            if (record.getResponseCode() == null) {
                throw new DuplicateIdempotencyException("A transaction with this idempotency key is currently processing.");
            }
            return Optional.of(record);
        }

        IdempotencyRecord newRecord = new IdempotencyRecord(
                key, userId, requestHash, LocalDateTime.now().plusMinutes(ttlMinutes)
        );
        idempotencyRepository.save(newRecord);
        return Optional.empty();
    }

    @Transactional
    public void recordSuccess(String key, String responseJson, int statusCode) {
        if (key == null || key.trim().isEmpty()) {
            return;
        }
        idempotencyRepository.findByKeyValue(key).ifPresent(record -> {
            record.setResponseBody(responseJson);
            record.setResponseCode(statusCode);
            idempotencyRepository.save(record);
        });
    }

    @Transactional
    public void releaseKey(String key) {
        if (key != null) {
            idempotencyRepository.findByKeyValue(key).ifPresent(idempotencyRepository::delete);
        }
    }
}
