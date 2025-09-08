package com.onlinebanking.portal.service;

import com.onlinebanking.portal.model.Account;
import com.onlinebanking.portal.model.Transaction;
import com.onlinebanking.portal.repository.AccountRepository;
import com.onlinebanking.portal.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction createTransaction(Long accountId, Transaction transaction) {
        Account account = accountRepository.findById(accountId).orElseThrow();

        if ("WITHDRAWAL".equalsIgnoreCase(transaction.getType()) && account.getBalance() < transaction.getAmount()) {
            throw new RuntimeException("Insufficient balance!");
        }

        if ("DEPOSIT".equalsIgnoreCase(transaction.getType())) {
            account.setBalance(account.getBalance() + transaction.getAmount());
        } else if ("WITHDRAWAL".equalsIgnoreCase(transaction.getType())) {
            account.setBalance(account.getBalance() - transaction.getAmount());
        }

        transaction.setAccount(account);
        transaction.setTimestamp(LocalDateTime.now());
        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }
}