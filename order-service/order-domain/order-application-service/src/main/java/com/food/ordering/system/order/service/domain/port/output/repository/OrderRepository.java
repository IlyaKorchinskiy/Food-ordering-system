package com.food.ordering.system.order.service.domain.port.output.repository;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {

    Order saveOrder(Order order);

    Optional<Order> findByOrderId(OrderId orderId);

    Optional<Order> findByTrackingId(TrackingId trackingId);
}
