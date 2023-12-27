package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
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
        OrderPaidEvent orderPaidEvent = completePaymentForOrder(paymentResponse);
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
        log.info("Order with id: {} is paid", orderPaidEvent.getOrder().getId().getValue());
    }

    @Override
    @Transactional
    // todo это мог быть просто метод cancel в OrderService, который принимает orderId и failureMessages, и не нужен
    // класс OrderPaymentSaga
    public void rollback(PaymentResponse paymentResponse) {
        Optional<OrderPaymentOutboxMessage> optionalOrderPaymentOutboxMessage =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(paymentResponse.getSagaId()),
                        getCurrentSagaStatuses(paymentResponse.getPaymentStatus()));
        if (optionalOrderPaymentOutboxMessage.isEmpty()) {
            log.info("An outbox message with saga id: {} is already roll backed.", paymentResponse.getSagaId());
            return;
        }
        OrderPaymentOutboxMessage paymentOutboxMessage = optionalOrderPaymentOutboxMessage.get();
        Order order = rollbackPaymentForOrder(paymentResponse);
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
        paymentOutboxHelper.save(
                getUpdatedPaymentOutboxMessage(paymentOutboxMessage, order.getOrderStatus(), sagaStatus));
        if (paymentResponse.getPaymentStatus() == PaymentStatus.CANCELLED) {
            approvalOutboxHelper.save(
                    getUpdatedApprovalOutboxMessage(paymentResponse.getSagaId(), order.getOrderStatus(), sagaStatus));
        }
        log.info("Order with id: {} is cancelled", order.getId().getValue());
    }

    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(
            String sagaId, OrderStatus orderStatus, SagaStatus sagaStatus) {
        Optional<OrderApprovalOutboxMessage> optionalOrderApprovalOutboxMessage =
                approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(sagaId), SagaStatus.COMPENSATING);
        if (optionalOrderApprovalOutboxMessage.isEmpty()) {
            throw new OrderDomainException(
                    "Approval outbox message could not be found in " + SagaStatus.COMPENSATING.name() + " status.");
        }
        OrderApprovalOutboxMessage orderApprovalOutboxMessage = optionalOrderApprovalOutboxMessage.get();
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        return orderApprovalOutboxMessage;
    }

    private Order rollbackPaymentForOrder(PaymentResponse paymentResponse) {
        log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        return order;
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(
            OrderPaymentOutboxMessage paymentOutboxMessage, OrderStatus orderStatus, SagaStatus sagaStatus) {
        paymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        paymentOutboxMessage.setOrderStatus(orderStatus);
        paymentOutboxMessage.setSagaStatus(sagaStatus);
        return paymentOutboxMessage;
    }

    private OrderPaidEvent completePaymentForOrder(PaymentResponse paymentResponse) {
        log.info("Completing payment for order with id: {}", paymentResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order);
        orderSagaHelper.saveOrder(order);
        return orderPaidEvent;
    }

    private SagaStatus[] getCurrentSagaStatuses(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case COMPLETED -> new SagaStatus[] {SagaStatus.STARTED};
            case CANCELLED -> new SagaStatus[] {SagaStatus.PROCESSING};
            case FAILED -> new SagaStatus[] {SagaStatus.STARTED, SagaStatus.PROCESSING};
        };
    }
}
