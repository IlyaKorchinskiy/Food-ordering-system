package com.food.ordering.system.order.service.domain.outbox.scheduler;

import com.food.orderin.system.saga.SagaStatus;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
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

    public PaymentOutboxHelper(PaymentOutboxRepository paymentOutboxRepository) {
        this.paymentOutboxRepository = paymentOutboxRepository;
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
}
