package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;
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
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {

    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderDataMapper orderDataMapper;

    public OrderApprovalSaga(
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
    public void process(RestaurantApprovalResponse restaurantApprovalResponse) {
        Optional<OrderApprovalOutboxMessage> optionalOrderApprovalOutboxMessage =
                approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(restaurantApprovalResponse.getSagaId()), SagaStatus.PROCESSING);
        if (optionalOrderApprovalOutboxMessage.isEmpty()) {
            log.info(
                    "An outbox message with saga id: {} is already processed.", restaurantApprovalResponse.getSagaId());
            return;
        }
        OrderApprovalOutboxMessage approvalOutboxMessage = optionalOrderApprovalOutboxMessage.get();
        Order order = approveOrder(restaurantApprovalResponse);
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
        approvalOutboxHelper.save(
                getUpdatedApprovalOutboxMessage(approvalOutboxMessage, order.getOrderStatus(), sagaStatus));
        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(
                restaurantApprovalResponse.getSagaId(), order.getOrderStatus(), sagaStatus));
        log.info("Order with id: {} is approved", order.getId().getValue());
    }

    @Override
    @Transactional
    public void rollback(RestaurantApprovalResponse restaurantApprovalResponse) {
        Optional<OrderApprovalOutboxMessage> optionalOrderApprovalOutboxMessage =
                approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(restaurantApprovalResponse.getSagaId()), SagaStatus.PROCESSING);
        if (optionalOrderApprovalOutboxMessage.isEmpty()) {
            log.info(
                    "An outbox message with saga id: {} is already roll backed.",
                    restaurantApprovalResponse.getSagaId());
            return;
        }
        OrderApprovalOutboxMessage approvalOutboxMessage = optionalOrderApprovalOutboxMessage.get();
        OrderCancelledEvent orderCancelledEvent = rollbackOrder(restaurantApprovalResponse);
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(
                orderCancelledEvent.getOrder().getOrderStatus());
        approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(
                approvalOutboxMessage, orderCancelledEvent.getOrder().getOrderStatus(), sagaStatus));
        paymentOutboxHelper.saveOutboxMessage(
                orderDataMapper.orderCancelledEventToOrderPaymentEventPayload(orderCancelledEvent),
                orderCancelledEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(restaurantApprovalResponse.getSagaId()));
        log.info(
                "Order with id: {} is cancelling.",
                orderCancelledEvent.getOrder().getId().getValue());
    }

    private OrderCancelledEvent rollbackOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
        log.info("Cancelling order with id: {}", restaurantApprovalResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
        OrderCancelledEvent orderCancelledEvent =
                orderDomainService.cancelOrderPayment(order, restaurantApprovalResponse.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        return orderCancelledEvent;
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(
            String sagaId, OrderStatus orderStatus, SagaStatus sagaStatus) {
        Optional<OrderPaymentOutboxMessage> optionalOrderPaymentOutboxMessage =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(sagaId), SagaStatus.PROCESSING);
        if (optionalOrderPaymentOutboxMessage.isEmpty()) {
            throw new OrderDomainException(
                    "Payment outbox message could not be found in " + SagaStatus.PROCESSING.name() + " status.");
        }
        OrderPaymentOutboxMessage paymentOutboxMessage = optionalOrderPaymentOutboxMessage.get();
        paymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        paymentOutboxMessage.setOrderStatus(orderStatus);
        paymentOutboxMessage.setSagaStatus(sagaStatus);
        return paymentOutboxMessage;
    }

    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(
            OrderApprovalOutboxMessage approvalOutboxMessage, OrderStatus orderStatus, SagaStatus sagaStatus) {
        approvalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        approvalOutboxMessage.setOrderStatus(orderStatus);
        approvalOutboxMessage.setSagaStatus(sagaStatus);
        return approvalOutboxMessage;
    }

    private Order approveOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
        log.info("Approving order with id: {}", restaurantApprovalResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
        orderDomainService.approveOrder(order);
        orderSagaHelper.saveOrder(order);
        return order;
    }
}
