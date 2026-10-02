package com.wallet.repo;

import com.wallet.model.TransferAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferAuditLogRepo extends JpaRepository<TransferAuditLog, Long> {
}
