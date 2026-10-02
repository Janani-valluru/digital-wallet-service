package com.wallet.repo;

import com.wallet.model.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface TransactionHistoryRepo extends JpaRepository<TransactionHistory, Long> {
    Optional<TransactionHistory> findByTransactionReference(String transactionReference);

    long deleteByStatusAndCreatedAtBefore(String status, Instant cutoff);
}
