package com.github.mshourabi.concurrentaccountservice.service;

import com.github.mshourabi.concurrentaccountservice.enums.TransactionStatus;
import com.github.mshourabi.concurrentaccountservice.enums.TransactionType;
import com.github.mshourabi.concurrentaccountservice.model.dto.TransactionDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;


    @ParameterizedTest
    @CsvSource(
            value = {
                    "CREDIT, null, A, 100, hashcode-test-1",
                    "DEBIT, B, null, 100, hashcode-test-2",
                    "TRANSFER, C, D, 200, hashcode-test-3"
            },
            nullValues = "null"
    )
    public void test_success_transaction(
            TransactionType type, String sourceAccountId, String destinationAccountId, long amount, String requestHashCode) {

        Long balanceDestinationAccountBeforeTransaction = null;
        Long balanceSourceAccountBeforeTransaction = null;

        if (destinationAccountId != null) {
            balanceDestinationAccountBeforeTransaction = accountService.findAccountById(destinationAccountId).getBalance();
        }
        if (sourceAccountId != null) {
            balanceSourceAccountBeforeTransaction = accountService.findAccountById(sourceAccountId).getBalance();
        }

        String uuid = UUID.randomUUID().toString();

//        TransactionDto.CreateRequest creditRequest = new TransactionDto.CreateRequest(
//                uuid,
//                requestHashCode,
//                type,
//                amount,
//                sourceAccountId,
//                destinationAccountId);
//        TransactionDto.CreateResponse transaction = transactionService.createTransaction(creditRequest);
//        Assertions.assertEquals(transaction.status(), TransactionStatus.COMPLETED);
//
//        assetBalance(type, destinationAccountId, sourceAccountId, amount, balanceDestinationAccountBeforeTransaction, balanceSourceAccountBeforeTransaction);
    }


    @ParameterizedTest
    @CsvSource(
            value = {
                    "DEBIT, E, null, 501, hashcode-test-4",
                    "TRANSFER, F, G, 601, hashcode-test-5"
            },
            nullValues = "null"
    )
    public void test_validation_amount(
            TransactionType type, String sourceAccountId, String destinationAccountId, long amount, String requestHashCode) {

        String uuid = UUID.randomUUID().toString();

//        TransactionDto.CreateRequest creditRequest = new TransactionDto.CreateRequest(
//                uuid,
//                requestHashCode,
//                type,
//                amount,
//                sourceAccountId,
//                destinationAccountId);
//        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> transactionService.createTransaction(creditRequest));
//
//        assertEquals("Insufficient funds.", runtimeException.getMessage());
    }


    @ParameterizedTest
    @CsvSource(
            value = {
                    "CREDIT, null, H, 50, hashcode-test-6, transactionIdUUID-test-6, CREDIT, null, H, 50, hashcode-test-6, transactionIdUUID-test-6",
                    "DEBIT, I, null, 50, hashcode-test-7, transactionIdUUID-test-7, DEBIT, I, null, 50, hashcode-test-7, transactionIdUUID-test-7",
                    "TRANSFER, J, K, 50, hashcode-test-8, transactionIdUUID-test-8, TRANSFER, J, K, 50, hashcode-test-8, transactionIdUUID-test-8",
            },
            nullValues = "null"
    )
    public void test_idempotency(
            TransactionType typeTx1, String sourceAccountIdTx1, String destinationAccountIdTx1, long amountTx1, String requestHashCodeTx1, String transactionIdTx1,
            TransactionType typeTx2, String sourceAccountIdTx2, String destinationAccountIdTx2, long amountTx2, String requestHashCodeTx2, String transactionIdTx2) {

        Long balanceDestinationAccountBeforeTransaction = null;
        Long balanceSourceAccountBeforeTransaction = null;

        if (destinationAccountIdTx1 != null) {
            balanceDestinationAccountBeforeTransaction = accountService.findAccountById(destinationAccountIdTx1).getBalance();
        }
        if (sourceAccountIdTx1 != null) {
            balanceSourceAccountBeforeTransaction = accountService.findAccountById(sourceAccountIdTx1).getBalance();
        }

        TransactionDto.CreateRequest creditRequestTx1 = new TransactionDto.CreateRequest(
                transactionIdTx1,
                requestHashCodeTx1,
                typeTx1,
                amountTx1,
                sourceAccountIdTx1,
                destinationAccountIdTx1);
//        TransactionDto.CreateResponse transaction1 = transactionService.createTransaction(creditRequestTx1);
//        assertEquals(TransactionStatus.COMPLETED, transaction1.status());
//
//        TransactionDto.CreateRequest creditRequestTx2 = new TransactionDto.CreateRequest(
//                transactionIdTx2,
//                requestHashCodeTx2,
//                typeTx2,
//                amountTx2,
//                sourceAccountIdTx2,
//                destinationAccountIdTx2);
//        TransactionDto.CreateResponse transaction2 = transactionService.createTransaction(creditRequestTx2);
//        assertEquals(transaction1.id(), transaction2.id());
//        assetBalance(typeTx1, destinationAccountIdTx1, sourceAccountIdTx1, amountTx1, balanceDestinationAccountBeforeTransaction, balanceSourceAccountBeforeTransaction);
    }


    /**
     *
     * @param type
     * @param destinationAccountId
     * @param sourceAccountId
     * @param amount
     * @param balanceDestinationAccountBeforeTransaction
     * @param balanceSourceAccountBeforeTransaction
     */
    private void assetBalance(TransactionType type,
                              String destinationAccountId,
                              String sourceAccountId,
                              Long amount,
                              Long balanceDestinationAccountBeforeTransaction,
                              Long balanceSourceAccountBeforeTransaction) {
        switch (type) {
            case CREDIT -> {
                long balanceDestinationAccountAfterTransaction = accountService.findAccountById(destinationAccountId).getBalance();
                Assertions.assertEquals(balanceDestinationAccountBeforeTransaction + amount, balanceDestinationAccountAfterTransaction);
            }
            case DEBIT -> {
                long balanceSourceAccountAfterTransaction = accountService.findAccountById(sourceAccountId).getBalance();
                Assertions.assertEquals(balanceSourceAccountBeforeTransaction - amount, balanceSourceAccountAfterTransaction);
            }
            case TRANSFER -> {
                long balanceDestinationAccountAfterTransaction = accountService.findAccountById(destinationAccountId).getBalance();
                long balanceSourceAccountAfterTransaction = accountService.findAccountById(sourceAccountId).getBalance();

                Assertions.assertEquals(balanceDestinationAccountBeforeTransaction + amount, balanceDestinationAccountAfterTransaction);
                Assertions.assertEquals(balanceSourceAccountBeforeTransaction - amount, balanceSourceAccountAfterTransaction);
            }
        }
    }
}