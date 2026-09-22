package com.github.mshourabi.concurrentaccountservice;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
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


    @Bean(name = "transactionTaskExecutor")
    public Executor transactionTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("transaction-");
        executor.initialize();
        return executor;
    }
}
