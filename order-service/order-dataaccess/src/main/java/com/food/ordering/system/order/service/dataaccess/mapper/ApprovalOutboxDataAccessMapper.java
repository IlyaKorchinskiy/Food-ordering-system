package com.food.ordering.system.order.service.dataaccess.mapper;

import com.food.ordering.system.order.service.dataaccess.entity.ApprovalOutboxEntity;
import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class ApprovalOutboxDataAccessMapper {

    public ApprovalOutboxEntity approvalOutboxMessageToApprovalOutboxEntity(
            OrderApprovalOutboxMessage approvalOutboxMessage) {
        return ApprovalOutboxEntity.builder()
                .id(approvalOutboxMessage.getId())
                .sagaId(approvalOutboxMessage.getSagaId())
                .type(approvalOutboxMessage.getType())
                .createdAt(approvalOutboxMessage.getCreatedAt())
                .processedAt(approvalOutboxMessage.getProcessedAt())
                .outboxStatus(approvalOutboxMessage.getOutboxStatus())
                .orderStatus(approvalOutboxMessage.getOrderStatus())
                .payload(approvalOutboxMessage.getPayload())
                .sagaStatus(approvalOutboxMessage.getSagaStatus())
                .version(approvalOutboxMessage.getVersion())
                .build();
    }

    public OrderApprovalOutboxMessage approvalOutboxEntityToApprovalOutboxMessage(
            ApprovalOutboxEntity approvalOutboxEntity) {
        return OrderApprovalOutboxMessage.builder()
                .id(approvalOutboxEntity.getId())
                .sagaId(approvalOutboxEntity.getSagaId())
                .type(approvalOutboxEntity.getType())
                .createdAt(approvalOutboxEntity.getCreatedAt())
                .processedAt(approvalOutboxEntity.getProcessedAt())
                .outboxStatus(approvalOutboxEntity.getOutboxStatus())
                .orderStatus(approvalOutboxEntity.getOrderStatus())
                .payload(approvalOutboxEntity.getPayload())
                .sagaStatus(approvalOutboxEntity.getSagaStatus())
                .version(approvalOutboxEntity.getVersion())
                .build();
    }
}
