package com.food.ordering.system.order.service.domain.outbox.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.orderin.system.saga.SagaStatus;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.port.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.food.orderin.system.saga.SagaConstants.ORDER_SAGA_NAME;

@Slf4j
@Component
public class PaymentOutboxHelper {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ObjectMapper objectMapper;

    public PaymentOutboxHelper(PaymentOutboxRepository paymentOutboxRepository, ObjectMapper objectMapper) {
        this.paymentOutboxRepository = paymentOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<List<OrderPaymentOutboxMessage>> getPaymentOutboxMessagesByOutboxStatusAndSagaStatus(
            OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        return paymentOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(
                ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }

    @Transactional(readOnly = true)
    public Optional<OrderPaymentOutboxMessage> getPaymentOutboxMessageBySagaIdAndSagaStatus(
            UUID sagaId, SagaStatus... sagaStatuses) {
        return paymentOutboxRepository.findByTypeAndSagaIdAndSagaStatus(ORDER_SAGA_NAME, sagaId, sagaStatuses);
    }

    @Transactional
    public void save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
        OrderPaymentOutboxMessage saved = paymentOutboxRepository.save(orderPaymentOutboxMessage);
        if (saved == null) { // todo репозиторий не может вернуть null
            log.error("Could not save OrderPaymentOutboxMessage with outbox id: {}", orderPaymentOutboxMessage.getId());
            throw new OrderDomainException(
                    "Could not save OrderPaymentOutboxMessage with outbox id: " + orderPaymentOutboxMessage.getId());
        }
        log.info("OrderPaymentOutboxMessage saved with outbox id: {}", orderPaymentOutboxMessage.getId());
    }

    @Transactional
    public void saveOutboxMessage(
            OrderPaymentEventPayload orderPaymentEventPayload,
            OrderStatus orderStatus,
            SagaStatus sagaStatus,
            OutboxStatus outboxStatus,
            UUID sagaId) {
        save(OrderPaymentOutboxMessage.builder()
                .id(UUID.randomUUID())
                .outboxStatus(outboxStatus)
                .orderStatus(orderStatus)
                .sagaStatus(sagaStatus)
                .sagaId(sagaId)
                .type(ORDER_SAGA_NAME)
                .createdAt(orderPaymentEventPayload.getCreatedAt())
                .payload(createPayload(orderPaymentEventPayload))
                .build());
    }

    @Transactional
    public void deletePaymentOutboxMessagesByOutboxStatusAndSagaStatus(
            OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        paymentOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }

    private String createPayload(OrderPaymentEventPayload orderPaymentEventPayload) {
        try {
            return objectMapper.writeValueAsString(orderPaymentEventPayload);
        } catch (JsonProcessingException e) {
            log.error(
                    "Couldn't create OrderPaymentEventPayload object for order id: {}",
                    orderPaymentEventPayload.getOrderId(),
                    e);
            throw new OrderDomainException(
                    "Couldn't create OrderPaymentEventPayload object for order id: "
                            + orderPaymentEventPayload.getOrderId(),
                    e);
        }
    }
}
