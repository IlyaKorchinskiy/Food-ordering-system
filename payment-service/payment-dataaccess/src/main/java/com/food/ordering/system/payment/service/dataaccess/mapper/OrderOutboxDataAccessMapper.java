package com.food.ordering.system.payment.service.dataaccess.mapper;

import com.food.ordering.system.payment.service.dataaccess.entity.OrderOutboxEntity;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class OrderOutboxDataAccessMapper {

    public OrderOutboxEntity orderOutboxMessageToOrderOutboxEntity(OrderOutboxMessage orderOutboxMessage) {
        return OrderOutboxEntity.builder()
                .id(orderOutboxMessage.getId())
                .sagaId(orderOutboxMessage.getSagaId())
                .type(orderOutboxMessage.getType())
                .createdAt(orderOutboxMessage.getCreatedAt())
                .processedAt(orderOutboxMessage.getProcessedAt())
                .outboxStatus(orderOutboxMessage.getOutboxStatus())
                .paymentStatus(orderOutboxMessage.getPaymentStatus())
                .payload(orderOutboxMessage.getPayload())
                .version(orderOutboxMessage.getVersion())
                .build();
    }

    public OrderOutboxMessage orderOutboxEntityToOrderOutboxMessage(OrderOutboxEntity orderOutboxEntity) {
        return OrderOutboxMessage.builder()
                .id(orderOutboxEntity.getId())
                .sagaId(orderOutboxEntity.getSagaId())
                .type(orderOutboxEntity.getType())
                .createdAt(orderOutboxEntity.getCreatedAt())
                .processedAt(orderOutboxEntity.getProcessedAt())
                .outboxStatus(orderOutboxEntity.getOutboxStatus())
                .paymentStatus(orderOutboxEntity.getPaymentStatus())
                .payload(orderOutboxEntity.getPayload())
                .version(orderOutboxEntity.getVersion())
                .build();
    }
}
