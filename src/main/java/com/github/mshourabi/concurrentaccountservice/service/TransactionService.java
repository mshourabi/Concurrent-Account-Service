package com.github.mshourabi.concurrentaccountservice.service;

import com.github.mshourabi.concurrentaccountservice.enums.TransactionStatus;
import com.github.mshourabi.concurrentaccountservice.enums.TransactionType;
import com.github.mshourabi.concurrentaccountservice.model.dto.TransactionDto;
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
    private final TransactionService self;
    private final AccountService accountService;

    public TransactionService(TransactionRepository repository, TransactionService self, AccountService accountService) {
        this.repository = repository;
        this.self = self;
        this.accountService = accountService;
    }

    /**
     *
     * @param transactionId
     * @return
     */
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public Optional<Transaction> findTransactionById(String transactionId) {
        return repository.findByTransactionId(transactionId);
    }


    /**
     *
     * @param transaction
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public Transaction save(Transaction transaction) {
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
            default -> {
                throw new RuntimeException("invalid transaction type.");
            }
        }

        try {
            transaction.setStatus(TransactionStatus.COMPLETED);
            return repository.save(transaction);

        } catch (DataIntegrityViolationException ex) {
            Transaction concurrentTransaction = repository.findByTransactionId(transaction.getTransactionId()).orElseThrow(() -> ex);
            if (!concurrentTransaction.getRequestHash().equals(transaction.getRequestHash())) {
                throw new RuntimeException("The transactionId is already used by another request.");
            }
            return concurrentTransaction;

        }
    }


    /**
     *
     * @param createRequest
     * @return
     */
    public TransactionDto.CreateResponse createTransaction(TransactionDto.CreateRequest createRequest) {
        Transaction transaction = checkIfTransactionExists(createRequest);
        if  (transaction == null) {
            checkDestinationAccount(createRequest.type(), createRequest.destinationAccountId());
            checkBalanceInSourceAccount(createRequest.type(), createRequest.sourceAccountId(), createRequest.amount());
            transaction = TransactionDto.CreateRequest.map(createRequest);
            transaction = self.save(transaction);
        }
        return TransactionDto.CreateResponse.map(transaction);
    }


    private Account checkDestinationAccount(TransactionType type, Long destinationAccountId) {
        if (type.equals(TransactionType.TRANSFER) || type.equals(TransactionType.CREDIT)) {
            return accountService.findAccountById(destinationAccountId);
        }
        return null;
    }

    private Account checkBalanceInSourceAccount(TransactionType type, Long sourceAccountId, Long amount) {
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
     * @param createRequest
     * @return
     */
    private Transaction checkIfTransactionExists(TransactionDto.CreateRequest createRequest) {
        Optional<Transaction> optional = self.findTransactionById(createRequest.transactionId());

        if (optional.isPresent()) {
            Transaction transaction = optional.get();

            if (!transaction.getRequestHash().equals(createRequest.requestHashCode())) {
                throw new RuntimeException("The transactionId is duplicated.");
            } else {
                return optional.get();
            }
        }
        return null;
    }

}
