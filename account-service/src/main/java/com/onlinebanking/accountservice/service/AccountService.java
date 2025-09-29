package com.onlinebanking.accountservice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.onlinebanking.accountservice.model.Account;
import com.onlinebanking.accountservice.repository.AccountRepository;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final RestTemplate restTemplate;

    public AccountService(AccountRepository accountRepository, RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.restTemplate = restTemplate;
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
            Account savedAccount = accountRepository.save(account);
            
            // Record transaction
            recordTransaction(accountNumber, amount, "DEPOSIT", account.getUsername(), null, "Deposit to account");
            
            return savedAccount;
        }
        return null;
    }

    public Account withdraw(String accountNumber, Double amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account != null && account.getBalance() >= amount) {
            account.setBalance(account.getBalance() - amount);
            Account savedAccount = accountRepository.save(account);
            
            // Record transaction
            recordTransaction(accountNumber, -amount, "WITHDRAWAL", account.getUsername(), null, "Withdrawal from account");
            
            return savedAccount;
        }
        return null;
    }
    
    public boolean transfer(String fromAccountNumber, String toAccountNumber, Double amount) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber);
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber);
        
        if (fromAccount != null && toAccount != null && fromAccount.getBalance() >= amount) {
            fromAccount.setBalance(fromAccount.getBalance() - amount);
            toAccount.setBalance(toAccount.getBalance() + amount);
            
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            
            // Record transfer transactions
            recordTransferTransactions(fromAccountNumber, toAccountNumber, amount, 
                                     fromAccount.getUsername(), toAccount.getUsername(), "Transfer");
            
            return true;
        }
        return false;
    }
    
    public boolean accountExists(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        return account != null;
    }
    
    private void recordTransaction(String accountNumber, Double amount, String type, String username, String description, String defaultDescription) {
        try {
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("accountNumber", accountNumber);
            transaction.put("amount", amount);
            transaction.put("type", type);
            transaction.put("username", username);
            transaction.put("timestamp", java.time.LocalDateTime.now().toString());
            transaction.put("description", description != null ? description : defaultDescription);
            
            restTemplate.postForObject("http://localhost:8082/transactions", transaction, Object.class);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to record transaction: " + e.getMessage());
        }
    }
    
    private void recordTransferTransactions(String fromAccountNumber, String toAccountNumber, Double amount, 
                                          String fromUsername, String toUsername, String description) {
        try {
            Map<String, Object> transferData = new HashMap<>();
            transferData.put("fromAccountNumber", fromAccountNumber);
            transferData.put("toAccountNumber", toAccountNumber);
            transferData.put("amount", amount);
            transferData.put("fromUsername", fromUsername);
            transferData.put("toUsername", toUsername);
            transferData.put("description", description);
            
            restTemplate.postForObject("http://localhost:8082/transactions/transfer", transferData, Object.class);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to record transfer transactions: " + e.getMessage());
        }
    }
}
