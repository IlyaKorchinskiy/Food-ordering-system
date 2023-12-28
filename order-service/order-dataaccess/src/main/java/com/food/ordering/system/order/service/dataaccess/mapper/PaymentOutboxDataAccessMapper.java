package com.food.ordering.system.order.service.dataaccess.mapper;

import com.food.ordering.system.order.service.dataaccess.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxDataAccessMapper {

    public PaymentOutboxEntity paymentOutboxMessageToPaymentOutboxEntity(
            OrderPaymentOutboxMessage paymentOutboxMessage) {
        return PaymentOutboxEntity.builder()
                .id(paymentOutboxMessage.getId())
                .sagaId(paymentOutboxMessage.getSagaId())
                .type(paymentOutboxMessage.getType())
                .createdAt(paymentOutboxMessage.getCreatedAt())
                .processedAt(paymentOutboxMessage.getProcessedAt())
                .outboxStatus(paymentOutboxMessage.getOutboxStatus())
                .orderStatus(paymentOutboxMessage.getOrderStatus())
                .payload(paymentOutboxMessage.getPayload())
                .sagaStatus(paymentOutboxMessage.getSagaStatus())
                .version(paymentOutboxMessage.getVersion())
                .build();
    }

    public OrderPaymentOutboxMessage paymentOutboxEntityToPaymentOutboxMessage(
            PaymentOutboxEntity paymentOutboxEntity) {
        return OrderPaymentOutboxMessage.builder()
                .id(paymentOutboxEntity.getId())
                .sagaId(paymentOutboxEntity.getSagaId())
                .type(paymentOutboxEntity.getType())
                .createdAt(paymentOutboxEntity.getCreatedAt())
                .processedAt(paymentOutboxEntity.getProcessedAt())
                .outboxStatus(paymentOutboxEntity.getOutboxStatus())
                .orderStatus(paymentOutboxEntity.getOrderStatus())
                .payload(paymentOutboxEntity.getPayload())
                .sagaStatus(paymentOutboxEntity.getSagaStatus())
                .version(paymentOutboxEntity.getVersion())
                .build();
    }
}
