package com.onlinebanking.accountservice.controller;

import com.onlinebanking.accountservice.model.Account;
import com.onlinebanking.accountservice.service.AccountService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @GetMapping("/{id}")
    public Account getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @GetMapping("/user/{username}")
    public List<Account> getAccountsByUsername(@PathVariable String username) {
        return accountService.getAccountsByUsername(username);
    }
}
