package com.appsdeveloperblog.estore.transfers.dto;

import java.math.BigDecimal;

public record TransferRequest(String senderId, String recipientId, BigDecimal amount) {
}
