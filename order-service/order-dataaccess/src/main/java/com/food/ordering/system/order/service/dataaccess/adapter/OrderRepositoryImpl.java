package com.food.ordering.system.order.service.dataaccess.adapter;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.order.service.dataaccess.entity.OrderEntity;
import com.food.ordering.system.order.service.dataaccess.mapper.OrderDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.repository.OrderJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.port.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderDataAccessMapper orderDataAccessMapper;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository, OrderDataAccessMapper orderDataAccessMapper) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderDataAccessMapper = orderDataAccessMapper;
    }

    @Override
    public Order saveOrder(Order order) {
        OrderEntity orderEntity = orderJpaRepository.save(orderDataAccessMapper.orderToOrderEntity(order));
        return orderDataAccessMapper.orderEntityToOrder(orderEntity);
    }

    @Override
    public Optional<Order> findByOrderId(OrderId orderId) {
        Optional<OrderEntity> optional = orderJpaRepository.findById(orderId.getValue());
        return optional.map(orderDataAccessMapper::orderEntityToOrder);
    }

    @Override
    public Optional<Order> findByTrackingId(TrackingId trackingId) {
        Optional<OrderEntity> optional = orderJpaRepository.findByTrackingId(trackingId.getValue());
        return optional.map(orderDataAccessMapper::orderEntityToOrder);
    }
}
