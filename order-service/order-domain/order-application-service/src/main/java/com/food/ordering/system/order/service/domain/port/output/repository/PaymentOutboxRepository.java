package com.food.ordering.system.order.service.domain.port.output.repository;

import com.food.orderin.system.saga.SagaStatus;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentOutboxRepository {

    OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage orderPaymentOutboxMessage);

    // todo непонятно зачем optional для листа возвращать
    Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(
            String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses);

    Optional<OrderPaymentOutboxMessage> findByTypeAndSagaIdAndSagaStatus(
            String type, UUID sagaId, SagaStatus... sagaStatuses);

    void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses);
}
