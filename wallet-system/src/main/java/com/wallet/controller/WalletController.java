package com.wallet.controller;

import com.wallet.dto.BalanceResponse;
import com.wallet.dto.DoTransDto;
import com.wallet.dto.TransferResponse;
import com.wallet.service.ServiceCall;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final ServiceCall serviceCall;

    public WalletController(ServiceCall serviceCall) {
        this.serviceCall = serviceCall;
    }

    @PostMapping("/transfer")
    public TransferResponse transfer(@Valid @RequestBody DoTransDto request, Authentication authentication) {
        return serviceCall.doIntraTransfer(request, authentication.getName());
    }

    @GetMapping("/accounts/{accountNumber}/balance")
    public BalanceResponse getBalance(@PathVariable String accountNumber, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return serviceCall.getBalance(accountNumber, authentication.getName(), admin);
    }
}
