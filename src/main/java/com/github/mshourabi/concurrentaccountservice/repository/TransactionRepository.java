package com.github.mshourabi.concurrentaccountservice.repository;

import com.github.mshourabi.concurrentaccountservice.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    Optional<Transaction> findByTransactionId(String transactionId);
}
