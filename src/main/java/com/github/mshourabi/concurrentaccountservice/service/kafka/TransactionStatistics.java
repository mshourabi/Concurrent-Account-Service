package com.github.mshourabi.concurrentaccountservice.service.kafka;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TransactionStatistics {

    /**
     * Statistics used to in test
     */
    public final AtomicInteger finished = new AtomicInteger();

    public final Map<String, Throwable> errors = new ConcurrentHashMap<>();

    public void reset() {
        finished.set(0);
        errors.clear();
    }
}
