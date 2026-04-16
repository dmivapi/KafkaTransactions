package com.appsdeveloperblog.payments.ws.core.events;

import java.math.BigDecimal;

public record WithdrawalRequestedEvent(String senderId, String recipientId, BigDecimal amount) {
}