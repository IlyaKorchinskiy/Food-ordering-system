package com.food.ordering.system.order.service.domain.outbox.scheduler;

import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PaymentOutboxCleanerScheduler implements OutboxScheduler {

    private final PaymentOutboxHelper paymentOutboxHelper;

    public PaymentOutboxCleanerScheduler(PaymentOutboxHelper paymentOutboxHelper) {
        this.paymentOutboxHelper = paymentOutboxHelper;
    }

    @Override
    @Transactional
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<OrderPaymentOutboxMessage>> optionalMessages =
                paymentOutboxHelper.getPaymentOutboxMessagesByOutboxStatusAndSagaStatuses(
                        OutboxStatus.COMPLETED, SagaStatus.SUCCEEDED, SagaStatus.COMPENSATED, SagaStatus.FAILED);
        if (optionalMessages.isPresent() && !optionalMessages.get().isEmpty()) {
            List<OrderPaymentOutboxMessage> messages = optionalMessages.get();
            log.info(
                    "Received {} OrderPaymentOutboxMessages to clean-up. The payloads: {}",
                    messages.size(),
                    messages.stream().map(OrderPaymentOutboxMessage::getPayload).collect(Collectors.joining("\n")));
            paymentOutboxHelper.deletePaymentOutboxMessagesByOutboxStatusAndSagaStatuses(
                    OutboxStatus.COMPLETED, SagaStatus.SUCCEEDED, SagaStatus.COMPENSATED, SagaStatus.FAILED);
            log.info("{} OrderPaymentOutboxMessages deleted.", messages.size());
        }
    }
}
