package com.appsdeveloperblog.payments.ws.core.events;

import java.math.BigDecimal;

public record DepositRequestedEvent(String senderId, String recipientId, BigDecimal amount) {
};