package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantDomainException;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.port.output.repository.OrderOutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.domain.DomainConstants.UTC;
import static com.food.ordering.system.saga.SagaConstants.ORDER_SAGA_NAME;

@Slf4j
@Component
public class OrderOutboxHelper {

    private final OrderOutboxRepository orderOutboxRepository;
    private final ObjectMapper objectMapper;

    public OrderOutboxHelper(OrderOutboxRepository orderOutboxRepository, ObjectMapper objectMapper) {
        this.orderOutboxRepository = orderOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<OrderOutboxMessage> getCompletedOrderOutboxMessageBySagaId(UUID sagaId) {
        return orderOutboxRepository.findByTypeAndSagaIdAndOutboxStatus(
                ORDER_SAGA_NAME, sagaId, OutboxStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public Optional<List<OrderOutboxMessage>> getOrderOutboxMessagesByOutboxStatus(OutboxStatus outboxStatus) {
        return orderOutboxRepository.findByTypeAndOutboxStatus(ORDER_SAGA_NAME, outboxStatus);
    }

    @Transactional
    public void deleteOrderOutboxMessagesByOutboxStatus(OutboxStatus outboxStatus) {
        orderOutboxRepository.deleteByTypeAndOutboxStatus(ORDER_SAGA_NAME, outboxStatus);
    }

    @Transactional
    public void updateOutboxMessage(OrderOutboxMessage orderOutboxMessage, OutboxStatus outboxStatus) {
        orderOutboxMessage.setOutboxStatus(outboxStatus);
        save(orderOutboxMessage);
        log.info("OrderOutboxMessage with id: {} is updated with status: {}", orderOutboxMessage.getId(), outboxStatus);
    }

    @Transactional
    public void saveOrderOutboxMessage(
            OrderEventPayload orderEventPayload,
            OrderApprovalStatus orderApprovalStatus,
            OutboxStatus outboxStatus,
            UUID sagaId) {
        save(OrderOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .outboxStatus(outboxStatus)
                .orderApprovalStatus(orderApprovalStatus)
                .type(ORDER_SAGA_NAME)
                .createdAt(orderEventPayload.getCreatedAt())
                .processedAt(ZonedDateTime.now(ZoneId.of(UTC)))
                .payload(createPayload(orderEventPayload))
                .build());
    }

    private String createPayload(OrderEventPayload orderEventPayload) {
        try {
            return objectMapper.writeValueAsString(orderEventPayload);
        } catch (JsonProcessingException e) {
            log.error("Couldn't create OrderEventPayload object for order id: {}", orderEventPayload.getOrderId(), e);
            throw new RestaurantDomainException(
                    "Couldn't create OrderEventPayload object for order id: " + orderEventPayload.getOrderId(), e);
        }
    }

    private void save(OrderOutboxMessage orderOutboxMessage) {
        OrderOutboxMessage saved = orderOutboxRepository.save(orderOutboxMessage);
        if (saved == null) { // todo репозиторий не может вернуть null
            log.error("Could not save OrderOutboxMessage with outbox id: {}", orderOutboxMessage.getId());
            throw new RestaurantDomainException(
                    "Could not save OrderOutboxMessage with outbox id: " + orderOutboxMessage.getId());
        }
        log.info("OrderOutboxMessage saved with outbox id: {}", orderOutboxMessage.getId());
    }
}
