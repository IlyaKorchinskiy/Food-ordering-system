package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.port.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderOutboxScheduler implements OutboxScheduler {

    private final OrderOutboxHelper orderOutboxHelper;
    private final RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher;

    public OrderOutboxScheduler(
            OrderOutboxHelper orderOutboxHelper,
            RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher) {
        this.orderOutboxHelper = orderOutboxHelper;
        this.restaurantApprovalResponseMessagePublisher = restaurantApprovalResponseMessagePublisher;
    }

    @Override
    @Transactional
    @Scheduled(
            fixedRateString = "${restaurant-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${restaurant-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<OrderOutboxMessage>> optionalOrderOutboxMessages =
                orderOutboxHelper.getOrderOutboxMessagesByOutboxStatus(OutboxStatus.STARTED);
        if (optionalOrderOutboxMessages.isPresent()
                && !optionalOrderOutboxMessages.get().isEmpty()) {
            List<OrderOutboxMessage> orderOutboxMessages = optionalOrderOutboxMessages.get();
            log.info(
                    "Received {} OrderOutboxMessages with ids: {}, sending to kafka.",
                    orderOutboxMessages.size(),
                    orderOutboxMessages.stream()
                            .map(orderOutboxMessage ->
                                    orderOutboxMessage.getId().toString())
                            .collect(Collectors.joining(",")));
            orderOutboxMessages.forEach(orderOutboxMessage -> restaurantApprovalResponseMessagePublisher.publish(
                    orderOutboxMessage, orderOutboxHelper::updateOutboxMessage));
            log.info("{} OrderOutboxMessages sent to message bus.", orderOutboxMessages.size());
        }
    }
}
