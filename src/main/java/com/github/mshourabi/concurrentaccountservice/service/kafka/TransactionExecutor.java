package com.github.mshourabi.concurrentaccountservice.service.kafka;

import com.github.mshourabi.concurrentaccountservice.model.dto.TransactionRequestDto;
import com.github.mshourabi.concurrentaccountservice.service.BalanceService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TransactionExecutor {

    private final BalanceService balanceService;
    private final TransactionStatistics transactionStatistics;

    public TransactionExecutor(BalanceService balanceService,  TransactionStatistics transactionStatistics) {
        this.balanceService = balanceService;
        this.transactionStatistics = transactionStatistics;
    }

    @Async("transactionTaskExecutor")
    public void execute(TransactionRequestDto dto) {
        try {
            switch (dto.type()) {
                case CREDIT -> balanceService.credit(dto.destinationAccountId(), dto.amount(), dto.transactionId());

                case DEBIT -> balanceService.debit(dto.sourceAccountId(), dto.amount(), dto.transactionId());

                case TRANSFER ->
                        balanceService.transfer(dto.sourceAccountId(), dto.destinationAccountId(), dto.amount(), dto.transactionId());

                default -> System.out.println("Invalid transaction type, transactionId: " + dto.transactionId());
            }
        } catch (RuntimeException ex) {
            transactionStatistics.errors.put(dto.transactionId() ,ex);
        } finally {
            transactionStatistics.finished.incrementAndGet();
        }
    }
}
