package com.onlinebanking.portal.controller;

import com.onlinebanking.portal.model.Transaction;
import com.onlinebanking.portal.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @PostMapping("/{accountId}")
    public Transaction createTransaction(@PathVariable Long accountId, @RequestBody Transaction transaction) {
        return transactionService.createTransaction(accountId, transaction);
    }
}