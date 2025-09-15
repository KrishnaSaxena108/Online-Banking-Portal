package com.onlinebanking.transactionservice.repository;

import com.onlinebanking.transactionservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUsername(String username);
    List<Transaction> findByAccountNumber(String accountNumber);
}
