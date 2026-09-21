package com.github.mshourabi.concurrentaccountservice.service;

import com.github.mshourabi.concurrentaccountservice.enums.TransactionType;
import com.github.mshourabi.concurrentaccountservice.model.entity.Account;
import com.github.mshourabi.concurrentaccountservice.model.entity.Transaction;
import org.springframework.stereotype.Service;

@Service
public class BalanceServiceImpl implements BalanceService {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public BalanceServiceImpl(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Override
    public void credit(String accountId, long amount, String transactionId) {
        TransactionType type = TransactionType.CREDIT;
        Transaction transaction = transactionService.checkIfTransactionExists(transactionId);
        if (transaction == null) {
            accountService.findAccountById(accountId);
            transaction = new Transaction(transactionId, null, type, amount, null, accountId);
            transactionService.executeTransaction(transaction);
        }
    }

    @Override
    public void debit(String accountId, long amount, String transactionId) {
        TransactionType type = TransactionType.DEBIT;
        Transaction transaction = transactionService.checkIfTransactionExists(transactionId);
        if (transaction == null) {
            Account account = accountService.findAccountById(accountId);
            if (account.getBalance() < amount) {
                throw new RuntimeException("Insufficient funds");
            }
            transaction = new Transaction(transactionId, null, type, amount, accountId, null);
            transactionService.executeTransaction(transaction);
        }
    }

    @Override
    public void transfer(String sourceAccountId, String destinationAccountId, long amount, String transactionId) {
        TransactionType type = TransactionType.TRANSFER;
        Transaction transaction = transactionService.checkIfTransactionExists(transactionId);
        if (transaction == null) {
            accountService.findAccountById(sourceAccountId);
            accountService.findAccountById(destinationAccountId);
            transaction = new Transaction(transactionId, null, type, amount, sourceAccountId, destinationAccountId);
            transactionService.executeTransaction(transaction);
        }
    }

    @Override
    public long getBalance(String accountId) {
        return accountService.findAccountById(accountId).getBalance();
    }

}
