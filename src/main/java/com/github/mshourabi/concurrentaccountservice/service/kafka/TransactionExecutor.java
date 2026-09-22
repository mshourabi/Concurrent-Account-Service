package com.github.mshourabi.concurrentaccountservice.service.kafka;

import com.github.mshourabi.concurrentaccountservice.model.dto.TransactionRequestDto;
import com.github.mshourabi.concurrentaccountservice.service.BalanceService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TransactionExecutor {

    private final BalanceService balanceService;

    public TransactionExecutor(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @Async("transactionTaskExecutor")
    public void execute(TransactionRequestDto dto) {

        switch (dto.type()) {
            case CREDIT -> balanceService.credit(dto.destinationAccount(), dto.amount(), dto.transactionId());

            case DEBIT -> balanceService.debit(dto.sourceAccount(), dto.amount(), dto.transactionId());

            case TRANSFER ->
                    balanceService.transfer(dto.sourceAccount(), dto.destinationAccount(), dto.amount(), dto.transactionId());

            default -> System.out.println("Invalid transaction type, transactionId: " + dto.transactionId());
        }
    }
}