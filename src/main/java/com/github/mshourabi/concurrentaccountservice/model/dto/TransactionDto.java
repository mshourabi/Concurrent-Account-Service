package com.github.mshourabi.concurrentaccountservice.model.dto;

import com.github.mshourabi.concurrentaccountservice.enums.TransactionStatus;
import com.github.mshourabi.concurrentaccountservice.enums.TransactionType;
import com.github.mshourabi.concurrentaccountservice.model.entity.Transaction;

import java.time.LocalDateTime;

public class TransactionDto {

    public record CreateRequest(
            String transactionId,
            String requestHashCode,
            TransactionType type,
            Long amount,
            String sourceAccountId,
            String destinationAccountId) {


        public static Transaction map(CreateRequest createRequest) {
            return new Transaction(createRequest.transactionId(),
                    createRequest.requestHashCode(),
                    createRequest.type(),
                    createRequest.amount(),
                    createRequest.sourceAccountId(),
                    createRequest.destinationAccountId());
        }

    }

    public record CreateResponse(
            Long id,
            String transactionId,
            TransactionType type,
            TransactionStatus status,
            String sourceAccountId,
            String destinationAccountId,
            Long amount,
            LocalDateTime createdAt
    ) {

        public static CreateResponse map(Transaction transaction) {
            LocalDateTime createdAt = LocalDateTime.from(transaction.getCreatedAt());
            return new CreateResponse(
                    transaction.getId(),
                    transaction.getTransactionId(),
                    transaction.getType(),
                    transaction.getStatus(),
                    transaction.getSourceAccountId(),
                    transaction.getDestinationAccountId(),
                    transaction.getAmount(),
                    createdAt);
        }
    }

}
