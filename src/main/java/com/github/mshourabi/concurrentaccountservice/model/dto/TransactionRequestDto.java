package com.github.mshourabi.concurrentaccountservice.model.dto;

import com.github.mshourabi.concurrentaccountservice.enums.TransactionType;

public record TransactionRequestDto(
        String sourceAccountId,
        String destinationAccountId,
        TransactionType type,
        long amount,
        String transactionId
) {
}
