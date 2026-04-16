package com.appsdeveloperblog.estore.transfers.service;

import com.appsdeveloperblog.estore.transfers.entity.TransferEntity;
import com.appsdeveloperblog.estore.transfers.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.appsdeveloperblog.estore.transfers.exception.TransferServiceException;
import com.appsdeveloperblog.estore.transfers.dto.TransferRequest;
import com.appsdeveloperblog.payments.ws.core.events.DepositRequestedEvent;
import com.appsdeveloperblog.payments.ws.core.events.WithdrawalRequestedEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    @Value("${app.kafka.deposit-money-topic}")
    private String depositTopicName;

    @Value("${app.kafka.withdraw-money-topic}")
    private String withdrawTopicName;

    private final TransferRepository transferRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestClient restClient;

    @Transactional("transactionManager")
    @Override
    public boolean transfer(TransferRequest transferRequest) {
        WithdrawalRequestedEvent withdrawalEvent = new WithdrawalRequestedEvent(transferRequest.senderId(),
                transferRequest.recipientId(), transferRequest.amount());
        DepositRequestedEvent depositEvent = new DepositRequestedEvent(transferRequest.senderId(),
                transferRequest.recipientId(), transferRequest.amount());
        TransferEntity transferEntity = TransferEntity.builder()
                .transferId(UUID.randomUUID().toString())
                .senderId(transferRequest.senderId())
                .recipientId(transferRequest.recipientId())
                .amount(transferRequest.amount())
                .build();
        try {
            // Save record to a database table
            transferRepository.save(transferEntity);

            kafkaTemplate.send(withdrawTopicName, withdrawalEvent);
            log.info("Sent event to withdrawal topic.");

            // Business logic that causes and error
            callRemoteService();

            kafkaTemplate.send(depositTopicName, depositEvent);
            log.info("Sent event to deposit topic");

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new TransferServiceException(ex);
        }

        return true;
    }

    private ResponseEntity<String> callRemoteService() throws Exception {
        String requestUrl = "http://localhost:8082/response/200";
        ResponseEntity<String> response = restClient.get().uri(requestUrl).retrieve().toEntity(String.class);

        if (response.getStatusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            throw new Exception("Destination Microservice not available");
        }

        if (response.getStatusCode().value() == HttpStatus.OK.value()) {
            log.info("Received response from mock service: {}", response.getBody());
        }
        return response;
    }

}
