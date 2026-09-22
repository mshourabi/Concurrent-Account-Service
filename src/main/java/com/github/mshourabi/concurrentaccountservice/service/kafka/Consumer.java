package com.github.mshourabi.concurrentaccountservice.service.kafka;

import com.github.mshourabi.concurrentaccountservice.ConcurrentAccountServiceApplication;
import com.github.mshourabi.concurrentaccountservice.model.dto.TransactionRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class Consumer {

    @Autowired
    private TransactionExecutor transactionExecutor;

    @KafkaListener(
            topics = ConcurrentAccountServiceApplication.ACCOUNT_TRANSACTIONS_TOPIC,
            groupId = "account-service-group"
    )
    public void consume(TransactionRequestDto dto) {
        transactionExecutor.execute(dto);
    }

}
