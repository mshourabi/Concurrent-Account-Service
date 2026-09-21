package com.github.mshourabi.concurrentaccountservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    BalanceService balanceService;

    @Autowired
    AccountService accountService;

    @BeforeEach
    void setUp() {
    }


    @ParameterizedTest()
    @CsvSource(
            value = {
                    "O, 100, 2, 2"
            }
    )
    void when_many_debit_request_try_change_an_account_balance(
            String sourceAccountId,
            long transactionAmount,
            int numberOfRequests,
            int threadNumbers) throws InterruptedException {

        long balanceBeforeTransactions = balanceService.getBalance(sourceAccountId);

        ExecutorService executor = Executors.newFixedThreadPool(threadNumbers);

        CountDownLatch readyLatch = new CountDownLatch(numberOfRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfRequests);

        AtomicInteger successfulTransactions = new AtomicInteger();
        AtomicInteger failedTransactions = new AtomicInteger();
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {

                    readyLatch.countDown();
                    startLatch.await();
                    String uuid = UUID.randomUUID().toString();
                    balanceService.debit(sourceAccountId, transactionAmount, uuid);
                    successfulTransactions.incrementAndGet();

                } catch (Throwable ex) {

                    errors.add(ex);
                    failedTransactions.incrementAndGet();

                } finally {

                    doneLatch.countDown();

                }
            });
        }

        assertTrue(readyLatch.await(5, TimeUnit.SECONDS));

        startLatch.countDown();

        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        long finalBalance = balanceService.getBalance(sourceAccountId);

        assertEquals(1, successfulTransactions.get());
        assertEquals(1, failedTransactions.get());
        assertEquals(balanceBeforeTransactions - transactionAmount, finalBalance);
        assertTrue(errors.stream().allMatch(ex -> ex instanceof ObjectOptimisticLockingFailureException));
    }


    @ParameterizedTest()
    @CsvSource(
            value = {
                    "P, 100, 2, 2"
            }
    )
    void when_many_credit_request_try_change_an_account_balance(
            String destinationAccountId,
            long transactionAmount,
            int numberOfRequests,
            int threadNumbers) throws InterruptedException {

        long balanceBeforeTransactions = balanceService.getBalance(destinationAccountId);

        ExecutorService executor = Executors.newFixedThreadPool(threadNumbers);

        CountDownLatch readyLatch = new CountDownLatch(numberOfRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfRequests);

        AtomicInteger successfulTransactions = new AtomicInteger();
        AtomicInteger failedTransactions = new AtomicInteger();
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {

                    readyLatch.countDown();
                    startLatch.await();
                    String uuid = UUID.randomUUID().toString();
                    balanceService.credit(destinationAccountId, transactionAmount, uuid);
                    successfulTransactions.incrementAndGet();

                } catch (Throwable ex) {

                    errors.add(ex);
                    failedTransactions.incrementAndGet();

                } finally {

                    doneLatch.countDown();

                }
            });
        }

        assertTrue(readyLatch.await(5, TimeUnit.SECONDS));

        startLatch.countDown();

        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        long finalBalance = balanceService.getBalance(destinationAccountId);

        assertEquals(1, successfulTransactions.get());
        assertEquals(1, failedTransactions.get());
        assertEquals(balanceBeforeTransactions + transactionAmount, finalBalance);
        assertTrue(errors.stream().allMatch(ex -> ex instanceof ObjectOptimisticLockingFailureException));
    }

    @ParameterizedTest()
    @CsvSource(
            value = {
                    "Q, R, S, 100, 2, 2"
            }
    )
    void when_two_transfer_request_from_two_different_source_account_to_one_destination(
            String sourceAccountId_1,
            String sourceAccountId_2,
            String destinationAccountId,
            long transactionAmount,
            int numberOfRequests,
            int threadNumbers) throws InterruptedException {

        long balanceDestinationBeforeTransactions = balanceService.getBalance(destinationAccountId);
        long balanceSourceBeforeTransactions_1 = balanceService.getBalance(sourceAccountId_1);
        long balanceSourceBeforeTransactions_2 = balanceService.getBalance(sourceAccountId_2);

        ExecutorService executor = Executors.newFixedThreadPool(threadNumbers);

        CountDownLatch readyLatch = new CountDownLatch(numberOfRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfRequests);

        AtomicInteger successfulTransactions = new AtomicInteger();
        AtomicInteger failedTransactions = new AtomicInteger();
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {
            String sourceAccountId = i == 1 ? sourceAccountId_1 : sourceAccountId_2;

            executor.submit(() -> {
                try {

                    readyLatch.countDown();
                    startLatch.await();
                    String uuid = UUID.randomUUID().toString();
                    balanceService.transfer(sourceAccountId, destinationAccountId, transactionAmount, uuid);
                    successfulTransactions.incrementAndGet();

                } catch (Throwable ex) {

                    errors.add(ex);
                    failedTransactions.incrementAndGet();

                } finally {

                    doneLatch.countDown();

                }
            });
        }

        assertTrue(readyLatch.await(5, TimeUnit.SECONDS));

        startLatch.countDown();

        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        long finalBalanceDestination = balanceService.getBalance(destinationAccountId);
        long finalBalanceSourceAccount_1 = balanceService.getBalance(sourceAccountId_1);
        long finalBalanceSourceAccount_2 = balanceService.getBalance(sourceAccountId_2);

        assertEquals(1, successfulTransactions.get());
        assertEquals(1, failedTransactions.get());
        assertEquals(balanceDestinationBeforeTransactions + transactionAmount, finalBalanceDestination);
        assertTrue(errors.stream().allMatch(ex -> ex instanceof ObjectOptimisticLockingFailureException));

        assertTrue((balanceSourceBeforeTransactions_1  == finalBalanceSourceAccount_1
                && balanceSourceBeforeTransactions_2 - transactionAmount == finalBalanceSourceAccount_2)
                ||
                (balanceSourceBeforeTransactions_1 - transactionAmount  == finalBalanceSourceAccount_1
                        && balanceSourceBeforeTransactions_2 == finalBalanceSourceAccount_2));
    }

}
