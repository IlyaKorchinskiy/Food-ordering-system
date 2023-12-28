package com.food.ordering.system.order.service.dataaccess.adapter;

import com.food.ordering.system.order.service.dataaccess.entity.ApprovalOutboxEntity;
import com.food.ordering.system.order.service.dataaccess.exception.ApprovalOutboxNotFoundException;
import com.food.ordering.system.order.service.dataaccess.mapper.ApprovalOutboxDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.repository.ApprovalOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.port.output.repository.ApprovalOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ApprovalOutboxRepositoryImpl implements ApprovalOutboxRepository {

    private final ApprovalOutboxJpaRepository approvalOutboxJpaRepository;
    private final ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper;

    public ApprovalOutboxRepositoryImpl(
            ApprovalOutboxJpaRepository approvalOutboxJpaRepository,
            ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper) {
        this.approvalOutboxJpaRepository = approvalOutboxJpaRepository;
        this.approvalOutboxDataAccessMapper = approvalOutboxDataAccessMapper;
    }

    @Override
    public OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
        ApprovalOutboxEntity approvalOutboxEntity =
                approvalOutboxDataAccessMapper.approvalOutboxMessageToApprovalOutboxEntity(orderApprovalOutboxMessage);
        approvalOutboxEntity = approvalOutboxJpaRepository.save(approvalOutboxEntity);
        return approvalOutboxDataAccessMapper.approvalOutboxEntityToApprovalOutboxMessage(approvalOutboxEntity);
    }

    @Override
    public Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatuseses(
            String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        List<OrderApprovalOutboxMessage> approvalOutboxMessages = approvalOutboxJpaRepository
                .findByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, List.of(sagaStatuses))
                .orElseThrow(() -> new ApprovalOutboxNotFoundException(
                        "Approval outbox object could not be found for saga type " + type))
                .stream()
                .map(approvalOutboxDataAccessMapper::approvalOutboxEntityToApprovalOutboxMessage)
                .collect(Collectors.toList());
        return Optional.of(approvalOutboxMessages);
    }

    @Override
    public Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatuses(
            String type, UUID sagaId, SagaStatus... sagaStatuses) {
        return approvalOutboxJpaRepository
                .findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, List.of(sagaStatuses))
                .map(approvalOutboxDataAccessMapper::approvalOutboxEntityToApprovalOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatusAndSagaStatuses(
            String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        approvalOutboxJpaRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(
                type, outboxStatus, List.of(sagaStatuses));
    }
}
