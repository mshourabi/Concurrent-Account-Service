package com.github.mshourabi.concurrentaccountservice.model.dto;

import com.github.mshourabi.concurrentaccountservice.enums.TransactionStatus;
import com.github.mshourabi.concurrentaccountservice.enums.TransactionType;
import com.github.mshourabi.concurrentaccountservice.model.entity.Transaction;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TransactionDto {

    public record CreateRequest(
            String transactionId,
            String requestHashCode,
            TransactionType type,
            Long amount,
            String sourceAccountId,
            String destinationAccountId) {

        public CreateRequest {
            if (!StringUtils.hasText(transactionId)) {
                throw new IllegalArgumentException("transactionId is not valid");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("amount is not valid");
            }
            if (sourceAccountId != null && sourceAccountId.equals(destinationAccountId)) {
                throw new IllegalArgumentException("sourceAccountId and destinationAccountId cannot be the same.");
            }
            if (TransactionType.CREDIT.equals(type) && !StringUtils.hasText(destinationAccountId)) {
                throw new IllegalArgumentException("In CREDIT Transaction destinationAccountId cannot be null.");
            }
            if (TransactionType.DEBIT.equals(type) && !StringUtils.hasText(sourceAccountId)) {
                throw new IllegalArgumentException("In DEBIT transaction sourceAccountId cannot be null.");
            }
            if (TransactionType.TRANSFER.equals(type) && (!StringUtils.hasText(sourceAccountId) || !StringUtils.hasText(destinationAccountId))) {
                throw new IllegalArgumentException("In TRANSFER transaction sourceAccountId and destinationAccountId cannot be null.");
            }

        }

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
            LocalDateTime createdAt = transaction.getCreatedAt().atZone(ZoneId.of("Asia/Tehran")).toLocalDateTime();
            ;
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
