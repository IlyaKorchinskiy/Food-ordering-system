package com.food.ordering.system.order.service.domain.outbox.scheduler;

import com.food.orderin.system.saga.SagaStatus;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.port.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PaymentOutboxScheduler implements OutboxScheduler {

    private final PaymentOutboxHelper paymentOutboxHelper;
    private final PaymentRequestMessagePublisher paymentRequestMessagePublisher;

    public PaymentOutboxScheduler(
            PaymentOutboxHelper paymentOutboxHelper, PaymentRequestMessagePublisher paymentRequestMessagePublisher) {
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.paymentRequestMessagePublisher = paymentRequestMessagePublisher;
    }

    @Override
    @Transactional
    @Scheduled(
            fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<OrderPaymentOutboxMessage>> optionalMessages =
                paymentOutboxHelper.getPaymentOutboxMessagesByOutboxStatusAndSagaStatus(
                        OutboxStatus.STARTED, SagaStatus.STARTED, SagaStatus.COMPENSATING);
        if (optionalMessages.isPresent() && !optionalMessages.get().isEmpty()) {
            List<OrderPaymentOutboxMessage> messages = optionalMessages.get();
            log.info(
                    "Received {} OrderPaymentOutboxMessages with ids: {}, sending to message bus.",
                    messages.size(),
                    messages.stream()
                            .map(orderPaymentOutboxMessage ->
                                    orderPaymentOutboxMessage.getId().toString())
                            .collect(Collectors.joining(",")));
            messages.forEach(orderPaymentOutboxMessage ->
                    paymentRequestMessagePublisher.publish(orderPaymentOutboxMessage, this::updateOutboxMessageStatus));
            log.info("{} OrderPaymentOutboxMessages sent to message bus.", messages.size());
        }
    }

    // todo несовсем понятно зачем городить BiConsumer, если можно просто из паблишера вызвать application-service и
    // обновить статус. Тогда не нужна и транзакция на processOutboxMessage
    private void updateOutboxMessageStatus(
            OrderPaymentOutboxMessage orderPaymentOutboxMessage, OutboxStatus outboxStatus) {
        orderPaymentOutboxMessage.setOutboxStatus(outboxStatus);
        paymentOutboxHelper.save(orderPaymentOutboxMessage);
        log.info("OrderPaymentOutboxMessage is updated with outbox status: {}", outboxStatus);
    }
}
