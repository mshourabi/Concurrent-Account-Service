package com.github.mshourabi.concurrentaccountservice.service.kafka;

import com.github.mshourabi.concurrentaccountservice.enums.TransactionType;
import com.github.mshourabi.concurrentaccountservice.model.dto.TransactionRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class KafkaTest {


    @Autowired
    private Producer producer;

    @Autowired
    private Consumer consumer;

    /**
     * Before this tests you must run :   docker-compose> docker compose up -d
     * docker-compose.yml is in docker-compose directory
     */

    @Test
    public void sendMessage() {
        TransactionRequestDto dto = new TransactionRequestDto(
                "A", null, TransactionType.CREDIT, 1000L, UUID.randomUUID().toString());
        producer.send(dto);
    }

}