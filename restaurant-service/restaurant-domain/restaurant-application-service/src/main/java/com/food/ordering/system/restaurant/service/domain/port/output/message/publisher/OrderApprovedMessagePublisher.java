package com.food.ordering.system.restaurant.service.domain.port.output.message.publisher;

import com.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import com.ood.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;

public interface OrderApprovedMessagePublisher extends DomainEventPublisher<OrderApprovedEvent> {
}