package com.food.ordering.system.order.service.dataaccess.adapter;

import com.food.ordering.system.order.service.dataaccess.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.dataaccess.exception.PaymentOutboxNotFoundException;
import com.food.ordering.system.order.service.dataaccess.mapper.PaymentOutboxDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.repository.PaymentOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.port.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

    private final PaymentOutboxJpaRepository paymentOutboxJpaRepository;
    private final PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper;

    public PaymentOutboxRepositoryImpl(
            PaymentOutboxJpaRepository paymentOutboxJpaRepository,
            PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper) {
        this.paymentOutboxJpaRepository = paymentOutboxJpaRepository;
        this.paymentOutboxDataAccessMapper = paymentOutboxDataAccessMapper;
    }

    @Override
    public OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
        PaymentOutboxEntity paymentOutboxEntity =
                paymentOutboxDataAccessMapper.paymentOutboxMessageToPaymentOutboxEntity(orderPaymentOutboxMessage);
        paymentOutboxEntity = paymentOutboxJpaRepository.save(paymentOutboxEntity);
        return paymentOutboxDataAccessMapper.paymentOutboxEntityToPaymentOutboxMessage(paymentOutboxEntity);
    }

    @Override
    public Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatuses(
            String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        List<OrderPaymentOutboxMessage> paymentOutboxMessages = paymentOutboxJpaRepository
                .findByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, List.of(sagaStatuses))
                .orElseThrow(() -> new PaymentOutboxNotFoundException(
                        "Payment outbox object could not be found for saga type " + type))
                .stream()
                .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToPaymentOutboxMessage)
                .collect(Collectors.toList());
        return Optional.of(paymentOutboxMessages);
    }

    @Override
    public Optional<OrderPaymentOutboxMessage> findByTypeAndSagaIdAndSagaStatuses(
            String type, UUID sagaId, SagaStatus... sagaStatuses) {
        return paymentOutboxJpaRepository
                .findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, List.of(sagaStatuses))
                .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToPaymentOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatusAndSagaStatuses(
            String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        paymentOutboxJpaRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(
                type, outboxStatus, List.of(sagaStatuses));
    }
}
