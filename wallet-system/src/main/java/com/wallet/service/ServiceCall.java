package com.wallet.service;

import com.wallet.dto.BalanceResponse;
import com.wallet.dto.CreateUserAccountRequest;
import com.wallet.dto.CreateUserAccountResponse;
import com.wallet.dto.DoTransDto;
import com.wallet.dto.TransferResponse;

public interface ServiceCall {

    CreateUserAccountResponse createUserAndAccount(CreateUserAccountRequest request);

    TransferResponse doIntraTransfer(DoTransDto request, String principalEmail);

    BalanceResponse getBalance(String accountNumber, String principalEmail, boolean admin);

}
