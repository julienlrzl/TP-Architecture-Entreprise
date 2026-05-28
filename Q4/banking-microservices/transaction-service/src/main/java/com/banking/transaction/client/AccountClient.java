package com.banking.transaction.client;

import com.banking.transaction.dto.AccountDto;
import com.banking.transaction.dto.BalanceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-service")
public interface AccountClient {

    @GetMapping("/accounts/{id}")
    AccountDto getAccount(@PathVariable("id") Long id);

    @PutMapping("/accounts/{id}/balance")
    AccountDto updateBalance(@PathVariable("id") Long id,
                             @RequestBody BalanceUpdateRequest request);
}
