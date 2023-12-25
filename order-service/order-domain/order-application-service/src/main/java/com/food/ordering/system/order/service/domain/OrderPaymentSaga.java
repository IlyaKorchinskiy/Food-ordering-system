package com.food.ordering.system.order.service.domain;

import com.food.orderin.system.saga.SagaStep;
import com.food.ordering.system.domain.events.EmptyEvent;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
// todo непонятно зачем нужные классы с названием Saga. Эти операции можно спокойно делать в сервисе.
// process - это payOrder, rollback - это cancelOrder
public class OrderPaymentSaga implements SagaStep<PaymentResponse, OrderPaidEvent, EmptyEvent> {

    private final OrderDomainService orderDomainService;
    private final OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher;
    private final OrderSagaHelper orderSagaHelper;

    public OrderPaymentSaga(
            OrderDomainService orderDomainService,
            OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher,
            OrderSagaHelper orderSagaHelper) {
        this.orderDomainService = orderDomainService;
        this.orderPaidRestaurantRequestMessagePublisher = orderPaidRestaurantRequestMessagePublisher;
        this.orderSagaHelper = orderSagaHelper;
    }

    @Override
    @Transactional
    public OrderPaidEvent process(PaymentResponse paymentResponse) {
        log.info("Completing payment for order with id: {}", paymentResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order, orderPaidRestaurantRequestMessagePublisher);
        orderSagaHelper.saveOrder(order);
        log.info("Order with id: {} is paid", order.getId().getValue());
        return orderPaidEvent;
    }

    @Override
    @Transactional
    // todo это мог быть просто метод cancel в OrderService, который принимает orderId и failureMessages, и не нужен
    // класс OrderPaymentSaga
    public EmptyEvent rollback(PaymentResponse paymentResponse) {
        log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        log.info("Order with id: {} is cancelled", order.getId().getValue());
        return EmptyEvent.INSTANCE;
    }
}
