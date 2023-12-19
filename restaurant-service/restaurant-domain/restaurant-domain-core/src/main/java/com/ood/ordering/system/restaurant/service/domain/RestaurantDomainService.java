package com.ood.ordering.system.restaurant.service.domain;

import com.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import com.ood.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.ood.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.ood.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.ood.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;

import java.util.List;

public interface RestaurantDomainService {

    OrderApprovalEvent validateOrder(
            Restaurant restaurant,
            List<String> failureMessages,
            DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher,
            DomainEventPublisher<OrderRejectedEvent> orderRejectedEventDomainEventPublisher);
}
