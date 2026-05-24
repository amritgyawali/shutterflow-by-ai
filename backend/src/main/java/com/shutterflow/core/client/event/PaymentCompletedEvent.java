package com.shutterflow.core.client.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class PaymentCompletedEvent extends ApplicationEvent {

    private final String clientId;
    private final BigDecimal amount;

    public PaymentCompletedEvent(Object source, String clientId, BigDecimal amount) {
        super(source);
        this.clientId = clientId;
        this.amount = amount;
    }
}
