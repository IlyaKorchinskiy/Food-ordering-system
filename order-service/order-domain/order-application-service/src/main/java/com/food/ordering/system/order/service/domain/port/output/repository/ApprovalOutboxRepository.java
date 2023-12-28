package com.food.ordering.system.order.service.domain.port.output.repository;

import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalOutboxRepository {

    OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage orderApprovalOutboxMessage);

    Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatuseses(
            String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses);

    Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatuses(
            String type, UUID sagaId, SagaStatus... sagaStatuses);

    void deleteByTypeAndOutboxStatusAndSagaStatuses(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses);
}
