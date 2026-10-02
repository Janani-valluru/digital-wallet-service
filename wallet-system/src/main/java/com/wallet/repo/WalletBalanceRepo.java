package com.wallet.repo;

import com.wallet.model.WalletBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletBalanceRepo extends JpaRepository<WalletBalance, Long> {
}
