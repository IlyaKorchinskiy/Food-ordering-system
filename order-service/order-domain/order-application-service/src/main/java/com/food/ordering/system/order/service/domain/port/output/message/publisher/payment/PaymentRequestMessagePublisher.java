package com.food.ordering.system.order.service.domain.port.output.message.publisher.payment;

import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface PaymentRequestMessagePublisher {

    void publish(
            OrderPaymentOutboxMessage orderPaymentOutboxMessage,
            BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback);
}
