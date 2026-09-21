package com.github.mshourabi.concurrentaccountservice.service.kafka;

import com.github.mshourabi.concurrentaccountservice.ConcurrentAccountServiceApplication;
import com.github.mshourabi.concurrentaccountservice.model.dto.TransactionRequestDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class Producer {

    private final KafkaTemplate<String, TransactionRequestDto> kafkaTemplate;

    public Producer(KafkaTemplate<String, TransactionRequestDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(TransactionRequestDto dto) {
        kafkaTemplate.send(ConcurrentAccountServiceApplication.ACCOUNT_TRANSACTIONS_TOPIC, dto.transactionId(), dto);
    }
}
