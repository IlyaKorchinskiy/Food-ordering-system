package com.food.ordering.system.order.service.domain;

import com.food.orderin.system.saga.SagaStatus;
import com.food.orderin.system.saga.SagaStep;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.domain.DomainConstants.UTC;

@Slf4j
@Component
// todo непонятно зачем нужные классы с названием Saga. Эти операции можно спокойно делать в сервисе.
// process - это payOrder, rollback - это cancelOrder
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {

    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderDataMapper orderDataMapper;

    public OrderPaymentSaga(
            OrderDomainService orderDomainService,
            OrderSagaHelper orderSagaHelper,
            PaymentOutboxHelper paymentOutboxHelper,
            ApprovalOutboxHelper approvalOutboxHelper,
            OrderDataMapper orderDataMapper) {
        this.orderDomainService = orderDomainService;
        this.orderSagaHelper = orderSagaHelper;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderDataMapper = orderDataMapper;
    }

    @Override
    @Transactional
    public void process(PaymentResponse paymentResponse) {
        Optional<OrderPaymentOutboxMessage> optionalOrderPaymentOutboxMessage =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(paymentResponse.getSagaId()), SagaStatus.STARTED);
        if (optionalOrderPaymentOutboxMessage.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed.", paymentResponse.getSagaId());
            return;
        }
        OrderPaymentOutboxMessage paymentOutboxMessage = optionalOrderPaymentOutboxMessage.get();

        log.info("Completing payment for order with id: {}", paymentResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order);
        orderSagaHelper.saveOrder(order);

        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(
                orderPaidEvent.getOrder().getOrderStatus());
        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(
                paymentOutboxMessage, orderPaidEvent.getOrder().getOrderStatus(), sagaStatus));

        approvalOutboxHelper.saveOutboxMessage(
                orderDataMapper.orderPaidEventToOrderApprovalEventPayload(orderPaidEvent),
                orderPaidEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(paymentResponse.getSagaId()));

        log.info("Order with id: {} is paid", order.getId().getValue());
    }

    @Override
    @Transactional
    // todo это мог быть просто метод cancel в OrderService, который принимает orderId и failureMessages, и не нужен
    // класс OrderPaymentSaga
    public void rollback(PaymentResponse paymentResponse) {
        log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        log.info("Order with id: {} is cancelled", order.getId().getValue());
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(
            OrderPaymentOutboxMessage paymentOutboxMessage, OrderStatus orderStatus, SagaStatus sagaStatus) {
        paymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        paymentOutboxMessage.setOrderStatus(orderStatus);
        paymentOutboxMessage.setSagaStatus(sagaStatus);
        return paymentOutboxMessage;
    }
}
