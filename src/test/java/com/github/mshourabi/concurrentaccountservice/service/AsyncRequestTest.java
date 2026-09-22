package com.github.mshourabi.concurrentaccountservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.mshourabi.concurrentaccountservice.ConcurrentAccountServiceApplication;
import com.github.mshourabi.concurrentaccountservice.enums.TransactionStatus;
import com.github.mshourabi.concurrentaccountservice.enums.TransactionType;
import com.github.mshourabi.concurrentaccountservice.model.dto.TransactionRequestDto;
import com.github.mshourabi.concurrentaccountservice.model.entity.Account;
import com.github.mshourabi.concurrentaccountservice.repository.AccountRepository;
import com.github.mshourabi.concurrentaccountservice.repository.TransactionRepository;
import com.github.mshourabi.concurrentaccountservice.service.kafka.TransactionStatistics;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AsyncRequestTest {

    private static final String TOPIC = ConcurrentAccountServiceApplication.ACCOUNT_TRANSACTIONS_TOPIC;


    @Autowired
    private KafkaTemplate<String, TransactionRequestDto> kafkaTemplate;

    @Autowired
    private TransactionStatistics transactionStatistics;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BalanceService balanceService;

    @BeforeEach
    void setUp() {
        // Database must be reset to the initial state.
        // Use @Sql, repository cleanup, or a dedicated test fixture.
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "T, 5, 10, U, 1000, 50, V, W, 300, 100"
            }
    )
    void shouldPreserveDatabaseIntegrityUnderConcurrentTransfers(
            String creditDestinationAccountId, long creditAmount, int creditNumberOfRequest,
            String debitSourceAccountId, long debitAmount, int debitNumberOfRequest,
            String transferSourceAccountId, String transferDestinationAccountId, long transferAmount, int transferNumberOf) {

        int totalNumberOfRequests = creditNumberOfRequest + debitNumberOfRequest + transferNumberOf;

        List<TransactionRequestDto> creditRequests = createCreditRequests(creditDestinationAccountId, creditAmount, creditNumberOfRequest);
        List<TransactionRequestDto> requests = new ArrayList<>(creditRequests);

        List<TransactionRequestDto> debitRequests = createDebitRequests(debitSourceAccountId, debitAmount, debitNumberOfRequest);
        requests.addAll(debitRequests);

        List<TransactionRequestDto> transferRequests = createTransferRequests(transferSourceAccountId, transferDestinationAccountId, transferAmount, transferNumberOf);
        requests.addAll(transferRequests);

        Map<String, Long> initialBalances = readAccountBalances();

        sendRequestsToKafka(requests);

        // Wait until all transactions have been processed.
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofMillis(200))
                .until(() -> transactionStatistics.finished.get() == totalNumberOfRequests);

        checkCreditTransactionDataIntegrity(creditDestinationAccountId, creditAmount, creditRequests, initialBalances);
        checkDebitTransactionDataIntegrity(debitSourceAccountId, debitAmount, debitRequests, initialBalances);
        checkTransferTransactionDataIntegrity(transferSourceAccountId, transferDestinationAccountId, transferAmount, transferRequests, initialBalances);


    }

    private void checkTransferTransactionDataIntegrity(String transferSourceAccountId, String transferDestinationAccountId,
                                                       long transferAmount, List<TransactionRequestDto> transferRequests,
                                                       Map<String, Long> initialBalances) {


        List<String> list = transferRequests.stream().map(TransactionRequestDto::transactionId).toList();
        long initialSourceBalance = initialBalances.get(transferSourceAccountId);
        long initialDestinationBalance = initialBalances.get(transferDestinationAccountId);

        long requestCompleted = transactionRepository.countByTransactionIdInAndStatus(list, TransactionStatus.COMPLETED);

        long finalSourceAmountBalance = balanceService.getBalance(transferSourceAccountId);
        long finalDestinationBalance = balanceService.getBalance(transferDestinationAccountId);

        Assertions.assertEquals(initialSourceBalance - (requestCompleted * transferAmount), finalSourceAmountBalance);
        Assertions.assertEquals(initialDestinationBalance + (requestCompleted * transferAmount), finalDestinationBalance);

    }

    private void checkDebitTransactionDataIntegrity(String debitSourceAccountId, long debitAmount,
                                                    List<TransactionRequestDto> debitRequests, Map<String, Long> initialBalances) {

        List<String> list = debitRequests.stream().map(TransactionRequestDto::transactionId).toList();
        long initialBalance = initialBalances.get(debitSourceAccountId);
        long requestCompleted = transactionRepository.countByTransactionIdInAndStatus(list, TransactionStatus.COMPLETED);
        long finalAmountBalance = balanceService.getBalance(debitSourceAccountId);

        Assertions.assertEquals(initialBalance - (requestCompleted * debitAmount), finalAmountBalance);
    }


    private void checkCreditTransactionDataIntegrity(String creditDestinationAccountId, long creditAmount,
                                                     List<TransactionRequestDto> creditRequests, Map<String, Long> initialBalances) {

        List<String> list = creditRequests.stream().map(TransactionRequestDto::transactionId).toList();
        long initialBalance = initialBalances.get(creditDestinationAccountId);
        long requestCompleted = transactionRepository.countByTransactionIdInAndStatus(list, TransactionStatus.COMPLETED);
        long finalAmountBalance = balanceService.getBalance(creditDestinationAccountId);

        Assertions.assertEquals(initialBalance + (requestCompleted * creditAmount), finalAmountBalance);
    }

    private List<TransactionRequestDto> createCreditRequests(String destinationAccountId, Long amount, int numberOfRequests) {
        List<TransactionRequestDto> requests = new ArrayList<>();
        for (int i = 0; i < numberOfRequests; i++) {
            requests.add(new TransactionRequestDto(null, destinationAccountId, TransactionType.CREDIT, amount, UUID.randomUUID().toString()));
        }
        return requests;
    }


    private List<TransactionRequestDto> createDebitRequests(String sourceAccountId, long amount, int numberOfRequests) {
        List<TransactionRequestDto> requests = new ArrayList<>();
        for (int i = 0; i < numberOfRequests; i++) {
            requests.add(new TransactionRequestDto(sourceAccountId, null, TransactionType.DEBIT, amount, UUID.randomUUID().toString()));
        }
        return requests;
    }

    private List<TransactionRequestDto> createTransferRequests(String sourceAccountId, String destinationAccountId, long amount, int numberOfRequests) {
        List<TransactionRequestDto> requests = new ArrayList<>();
        for (int i = 0; i < numberOfRequests; i++) {
            requests.add(new TransactionRequestDto(sourceAccountId, destinationAccountId, TransactionType.TRANSFER, amount, UUID.randomUUID().toString()));
        }
        return requests;
    }

    private void sendRequestsToKafka(List<TransactionRequestDto> requests) {

        List<CompletableFuture<SendResult<String, TransactionRequestDto>>> futures =
                requests.stream()
                        .map(request ->
                                kafkaTemplate.send(
                                        TOPIC,
                                        request.transactionId(),
                                        request
                                )
                        )
                        .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private Map<String, Long> readAccountBalances() {
        return accountRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Account::getId, Account::getBalance));
    }

    private Long calculateTotalBalance(Map<String, Long> balances) {
        return balances.values()
                .stream()
                .reduce(0L, Long::sum);
    }

}