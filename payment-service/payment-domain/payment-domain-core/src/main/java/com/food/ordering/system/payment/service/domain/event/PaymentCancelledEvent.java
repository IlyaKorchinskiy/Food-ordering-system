package com.food.ordering.system.payment.service.domain.event;

import com.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.entity.Payment;

import java.time.ZonedDateTime;
import java.util.Collections;

public class PaymentCancelledEvent extends PaymentEvent {

    private final DomainEventPublisher<PaymentCancelledEvent> domainEventPublisher;

    public PaymentCancelledEvent(
            Payment payment,
            ZonedDateTime createdAt,
            DomainEventPublisher<PaymentCancelledEvent> domainEventPublisher) {
        super(payment, createdAt, Collections.emptyList());
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void fire() {
        domainEventPublisher.publish(this);
    }
}
