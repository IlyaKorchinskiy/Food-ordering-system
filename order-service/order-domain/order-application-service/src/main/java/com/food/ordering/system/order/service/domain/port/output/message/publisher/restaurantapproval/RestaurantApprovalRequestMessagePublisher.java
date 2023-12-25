package com.food.ordering.system.order.service.domain.port.output.message.publisher.restaurantapproval;

import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface RestaurantApprovalRequestMessagePublisher {

    void publish(
            OrderApprovalOutboxMessage orderApprovalOutboxMessage,
            BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback);
}
