package com.github.mshourabi.concurrentaccountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ConcurrentAccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcurrentAccountServiceApplication.class, args);
    }

}
