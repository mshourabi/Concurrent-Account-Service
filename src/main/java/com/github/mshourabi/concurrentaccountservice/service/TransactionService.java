package com.github.mshourabi.concurrentaccountservice.service;

import com.github.mshourabi.concurrentaccountservice.enums.TransactionStatus;
import com.github.mshourabi.concurrentaccountservice.enums.TransactionType;
import com.github.mshourabi.concurrentaccountservice.model.entity.Account;
import com.github.mshourabi.concurrentaccountservice.model.entity.Transaction;
import com.github.mshourabi.concurrentaccountservice.repository.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository repository, AccountService accountService) {
        this.repository = repository;
        this.accountService = accountService;
    }


    /**
     *
     * @param transaction
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public Transaction executeTransaction(Transaction transaction) {
        Account destinationAccount = checkDestinationAccount(transaction.getType(), transaction.getDestinationAccountId());
        Account sourceAccount = checkBalanceInSourceAccount(transaction.getType(), transaction.getSourceAccountId(), transaction.getAmount());

        switch (transaction.getType()) {
            case DEBIT -> {
                sourceAccount.setBalance(sourceAccount.getBalance() - transaction.getAmount());
                accountService.save(sourceAccount);

            }
            case CREDIT -> {
                destinationAccount.setBalance(destinationAccount.getBalance() + transaction.getAmount());
                accountService.save(destinationAccount);
            }
            case TRANSFER -> {
                sourceAccount.setBalance(sourceAccount.getBalance() - transaction.getAmount());
                destinationAccount.setBalance(destinationAccount.getBalance() + transaction.getAmount());
                accountService.save(sourceAccount);
                accountService.save(destinationAccount);
            }
            default -> throw new RuntimeException("invalid transaction type.");
        }

        sleep(500);   // add this sleep for concurrency test - this provided more possibility concurrency access to DB record

        try {
            transaction.setStatus(TransactionStatus.COMPLETED);
            return repository.save(transaction);

        } catch (DataIntegrityViolationException ex) {
            return repository.findByTransactionId(transaction.getTransactionId()).orElseThrow(() -> ex);
        }
    }


    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Account checkDestinationAccount(TransactionType type, String destinationAccountId) {
        if (type.equals(TransactionType.TRANSFER) || type.equals(TransactionType.CREDIT)) {
            return accountService.findAccountById(destinationAccountId);
        }
        return null;
    }

    private Account checkBalanceInSourceAccount(TransactionType type, String sourceAccountId, Long amount) {
        if (type.equals(TransactionType.TRANSFER) || type.equals(TransactionType.DEBIT)) {
            Account sourceAccount = accountService.findAccountById(sourceAccountId);
            if (sourceAccount.getBalance() < amount) {
                throw new RuntimeException("Insufficient funds.");
            }
            return sourceAccount;
        }
        return null;
    }

    /**
     *
     * @param transactionId
     * @return
     */
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public Transaction checkIfTransactionExists(String transactionId) {
        Optional<Transaction> optional = repository.findByTransactionId(transactionId);
        return optional.orElse(null);
    }
}
