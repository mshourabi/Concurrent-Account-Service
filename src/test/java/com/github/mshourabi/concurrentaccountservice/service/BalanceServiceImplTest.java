package com.github.mshourabi.concurrentaccountservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BalanceServiceImplTest {

    @Autowired
    BalanceService balanceService;


    /**
     * Test wrote based on data in src/main/java/resources/data.sql file
     */

    @ParameterizedTest
    @CsvSource(
            value = {
                    "A, 100",
                    "B, 200"
            }
    )
    void test_getBalance(String accountId, long expectedAmount) {
        long actualAmount = balanceService.getBalance(accountId);
        assertEquals(expectedAmount, actualAmount);
    }


    @ParameterizedTest
    @CsvSource(
            value = {
                    "C, 100",
                    "D, 200"
            }
    )
    void test_when_credit_is_success(String accountId, long creditAmount) {
        long balanceBeforeTransaction = balanceService.getBalance(accountId);
        String uuid = UUID.randomUUID().toString();
        balanceService.credit(accountId, creditAmount, uuid);
        Assertions.assertEquals(balanceBeforeTransaction + creditAmount, balanceService.getBalance(accountId));
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "Z, 100"
            }
    )
    void test_credit_when_account_not_fount(String accountId, long creditAmount) {
        String uuid = UUID.randomUUID().toString();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> balanceService.credit(accountId, creditAmount, uuid));
        Assertions.assertTrue(ex.getMessage().contains("Account not found"));
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "E, 100, test-credit-idempotency"
            }
    )
    void test_idempotency_in_credit(String accountId, long creditAmount, String transactionId){
        long balanceBeforeTransaction = balanceService.getBalance(accountId);
        balanceService.credit(accountId, creditAmount, transactionId);
        balanceService.credit(accountId, creditAmount, transactionId);
        Assertions.assertEquals(balanceBeforeTransaction + creditAmount, balanceService.getBalance(accountId));
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "F, 10",
                    "G, 20"
            }
    )
    void test_when_debit_is_success(String accountId, long debitAmount) {
        long balanceBeforeTransaction = balanceService.getBalance(accountId);
        String uuid = UUID.randomUUID().toString();
        balanceService.debit(accountId, debitAmount, uuid);
        Assertions.assertEquals(balanceBeforeTransaction - debitAmount, balanceService.getBalance(accountId));
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "H, 910"
            }
    )
    void test_debit_when_source_account_does_not_enough_balance(String accountId, long debitAmount) {
        String uuid = UUID.randomUUID().toString();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> balanceService.debit(accountId, debitAmount, uuid));
        Assertions.assertTrue(ex.getMessage().contains("Insufficient funds"));
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "I, 100, test-debit-idempotency"
            }
    )
    void test_idempotency_in_debit(String accountId, long debitAmount, String transactionId){
        long balanceBeforeTransaction = balanceService.getBalance(accountId);
        balanceService.debit(accountId, debitAmount, transactionId);
        balanceService.debit(accountId, debitAmount, transactionId);
        Assertions.assertEquals(balanceBeforeTransaction - debitAmount, balanceService.getBalance(accountId));
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "I, J, 10",
                    "K, L, 20"
            }
    )
    void test_when_transfer_is_success(String sourceAccountId, String destinationAccountId, long transferAmount) {
        long balanceSourceAccountBeforeTransaction = balanceService.getBalance(sourceAccountId);
        long balanceDestinationAccountBeforeTransaction = balanceService.getBalance(destinationAccountId);
        String uuid = UUID.randomUUID().toString();
        balanceService.transfer(sourceAccountId, destinationAccountId, transferAmount, uuid);
        Assertions.assertEquals(balanceSourceAccountBeforeTransaction - transferAmount, balanceService.getBalance(sourceAccountId));
        Assertions.assertEquals(balanceDestinationAccountBeforeTransaction + transferAmount, balanceService.getBalance(destinationAccountId));
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "H, G, 910"
            }
    )
    void test_transfer_when_source_account_does_not_enough_balance(String sourceAccountId, String destinationAccountId, long transferAmount) {
        String uuid = UUID.randomUUID().toString();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> balanceService.transfer(sourceAccountId, destinationAccountId, transferAmount, uuid));
        Assertions.assertTrue(ex.getMessage().contains("Insufficient funds"));
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "M, N, 100, test-transfer-idempotency"
            }
    )
    void test_idempotency_in_transfer(String sourceAccountId, String destinationAccountId, long transferAmount, String transactionId){
        long balanceSourceAccountBeforeTransaction = balanceService.getBalance(sourceAccountId);
        long balanceDestinationAccountBeforeTransaction = balanceService.getBalance(destinationAccountId);
        balanceService.transfer(sourceAccountId, destinationAccountId, transferAmount, transactionId);
        balanceService.transfer(sourceAccountId, destinationAccountId, transferAmount, transactionId);
        Assertions.assertEquals(balanceSourceAccountBeforeTransaction - transferAmount, balanceService.getBalance(sourceAccountId));
        Assertions.assertEquals(balanceDestinationAccountBeforeTransaction + transferAmount, balanceService.getBalance(destinationAccountId));
    }
}