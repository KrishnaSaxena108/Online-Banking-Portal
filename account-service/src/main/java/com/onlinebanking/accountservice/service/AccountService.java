package com.onlinebanking.accountservice.service;

import com.onlinebanking.accountservice.model.Account;
import com.onlinebanking.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    public List<Account> getAccountsByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public Account deposit(String accountNumber, Double amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account != null) {
            account.setBalance(account.getBalance() + amount);
            return accountRepository.save(account);
        }
        return null;
    }

    public Account withdraw(String accountNumber, Double amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account != null && account.getBalance() >= amount) {
            account.setBalance(account.getBalance() - amount);
            return accountRepository.save(account);
        }
        return null;
    }
}
