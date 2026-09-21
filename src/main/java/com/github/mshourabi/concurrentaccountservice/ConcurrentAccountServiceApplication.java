package com.github.mshourabi.concurrentaccountservice;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
@EnableJpaAuditing
public class ConcurrentAccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcurrentAccountServiceApplication.class, args);
    }


    /**
     * Create kafka topic
     */
    public static final String ACCOUNT_TRANSACTIONS_TOPIC = "account-transactions";

    @Bean
    public NewTopic accountTransactionsTopic() {
        return TopicBuilder.name(ACCOUNT_TRANSACTIONS_TOPIC).build();
    }
}
