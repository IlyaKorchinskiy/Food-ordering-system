package com.food.ordering.system.order.service.domain.event;

import com.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;

import java.time.ZonedDateTime;

public class OrderCancelledEvent extends OrderEvent {

    private final DomainEventPublisher<OrderCancelledEvent> domainEventPublisher;

    public OrderCancelledEvent(
            Order order, ZonedDateTime createdAt, DomainEventPublisher<OrderCancelledEvent> domainEventPublisher) {
        super(order, createdAt);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void fire() {
        domainEventPublisher.publish(this);
    }
}
