package com.onlinebanking.transactionservice.controller;

import com.onlinebanking.transactionservice.model.Transaction;
import com.onlinebanking.transactionservice.service.TransactionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        return transactionService.createTransaction(transaction);
    }

    @GetMapping("/user/{username}")
    public List<Transaction> getTransactionsByUsername(@PathVariable String username) {
        return transactionService.getTransactionsByUsername(username);
    }

    @GetMapping("/account/{accountNumber}")
    public List<Transaction> getTransactionsByAccountNumber(@PathVariable String accountNumber) {
        return transactionService.getTransactionsByAccountNumber(accountNumber);
    }
}
