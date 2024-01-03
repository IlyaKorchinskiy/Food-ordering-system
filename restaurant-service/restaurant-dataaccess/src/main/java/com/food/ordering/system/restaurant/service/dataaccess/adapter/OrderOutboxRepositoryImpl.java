package com.food.ordering.system.restaurant.service.dataaccess.adapter;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.dataaccess.entity.OrderOutboxEntity;
import com.food.ordering.system.restaurant.service.dataaccess.exception.OrderOutboxNotFoundException;
import com.food.ordering.system.restaurant.service.dataaccess.mapper.OrderOutboxDataAccessMapper;
import com.food.ordering.system.restaurant.service.dataaccess.repository.OrderOutboxJpaRepository;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.port.output.repository.OrderOutboxRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {

    private final OrderOutboxJpaRepository orderOutboxJpaRepository;
    private final OrderOutboxDataAccessMapper orderOutboxDataAccessMapper;

    public OrderOutboxRepositoryImpl(
            OrderOutboxJpaRepository orderOutboxJpaRepository,
            OrderOutboxDataAccessMapper orderOutboxDataAccessMapper) {
        this.orderOutboxJpaRepository = orderOutboxJpaRepository;
        this.orderOutboxDataAccessMapper = orderOutboxDataAccessMapper;
    }

    @Override
    public OrderOutboxMessage save(OrderOutboxMessage orderOutboxMessage) {
        OrderOutboxEntity orderOutboxEntity =
                orderOutboxDataAccessMapper.orderOutboxMessageToOrderOutboxEntity(orderOutboxMessage);
        orderOutboxEntity = orderOutboxJpaRepository.save(orderOutboxEntity);
        return orderOutboxDataAccessMapper.orderOutboxEntityToOrderOutboxMessage(orderOutboxEntity);
    }

    @Override
    public Optional<List<OrderOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
        List<OrderOutboxMessage> orderOutboxMessages = orderOutboxJpaRepository
                .findByTypeAndOutboxStatus(type, outboxStatus)
                .orElseThrow(() -> new OrderOutboxNotFoundException(
                        "Approval outbox object cannot be found for saga type " + type))
                .stream()
                .map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage)
                .collect(Collectors.toList());
        return Optional.of(orderOutboxMessages);
    }

    @Override
    public Optional<OrderOutboxMessage> findByTypeAndSagaIdAndOutboxStatus(
            String type, UUID sagaId, OutboxStatus outboxStatus) {
        return orderOutboxJpaRepository
                .findByTypeAndSagaIdAndOutboxStatus(type, sagaId, outboxStatus)
                .map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
        orderOutboxJpaRepository.deleteByTypeAndOutboxStatus(type, outboxStatus);
    }
}
