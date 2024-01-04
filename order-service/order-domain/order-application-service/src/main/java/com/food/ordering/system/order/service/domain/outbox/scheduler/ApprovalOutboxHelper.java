package com.food.ordering.system.order.service.domain.outbox.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.port.output.repository.ApprovalOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.saga.SagaConstants.ORDER_SAGA_NAME;

@Slf4j
@Component
public class ApprovalOutboxHelper {

    private final ApprovalOutboxRepository approvalOutboxRepository;
    private final ObjectMapper objectMapper;

    public ApprovalOutboxHelper(ApprovalOutboxRepository approvalOutboxRepository, ObjectMapper objectMapper) {
        this.approvalOutboxRepository = approvalOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<List<OrderApprovalOutboxMessage>> getApprovalOutboxMessagesByOutboxStatusAndSagaStatuses(
            OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        return approvalOutboxRepository.findByTypeAndOutboxStatusAndSagaStatuses(
                ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }

    @Transactional(readOnly = true)
    public Optional<OrderApprovalOutboxMessage> getApprovalOutboxMessageBySagaIdAndSagaStatuses(
            UUID sagaId, SagaStatus... sagaStatuses) {
        return approvalOutboxRepository.findByTypeAndSagaIdAndSagaStatuses(ORDER_SAGA_NAME, sagaId, sagaStatuses);
    }

    @Transactional
    public void save(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
        OrderApprovalOutboxMessage saved = approvalOutboxRepository.save(orderApprovalOutboxMessage);
        if (saved == null) { // todo репозиторий не может вернуть null
            log.error(
                    "Could not save OrderApprovalOutboxMessage with outbox id: {}", orderApprovalOutboxMessage.getId());
            throw new OrderDomainException(
                    "Could not save OrderApprovalOutboxMessage with outbox id: " + orderApprovalOutboxMessage.getId());
        }
        log.info("OrderApprovalOutboxMessage saved with outbox id: {}", orderApprovalOutboxMessage.getId());
    }

    @Transactional
    public void saveOutboxMessage(
            OrderApprovalEventPayload orderApprovalEventPayload,
            OrderStatus orderStatus,
            SagaStatus sagaStatus,
            OutboxStatus outboxStatus,
            UUID sagaId) {
        save(OrderApprovalOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .type(ORDER_SAGA_NAME)
                .outboxStatus(outboxStatus)
                .sagaStatus(sagaStatus)
                .createdAt(orderApprovalEventPayload.getCreatedAt())
                .orderStatus(orderStatus)
                .payload(createPayload(orderApprovalEventPayload))
                .build());
    }

    @Transactional
    public void deleteApprovalOutboxMessagesByOutboxStatusAndSagaStatuses(
            OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        approvalOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatuses(
                ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }

    private String createPayload(OrderApprovalEventPayload orderApprovalEventPayload) {
        try {
            return objectMapper.writeValueAsString(orderApprovalEventPayload);
        } catch (JsonProcessingException e) {
            log.error(
                    "Couldn't create OrderApprovalEventPayload object for order id: {}",
                    orderApprovalEventPayload.getOrderId(),
                    e);
            throw new OrderDomainException(
                    "Couldn't create OrderApprovalEventPayload object for order id: "
                            + orderApprovalEventPayload.getOrderId(),
                    e);
        }
    }
}
