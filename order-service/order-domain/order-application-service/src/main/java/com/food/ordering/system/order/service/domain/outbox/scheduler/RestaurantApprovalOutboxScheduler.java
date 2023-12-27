package com.food.ordering.system.order.service.domain.outbox.scheduler;

import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.port.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
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
public class RestaurantApprovalOutboxScheduler implements OutboxScheduler {

    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher;

    public RestaurantApprovalOutboxScheduler(
            ApprovalOutboxHelper approvalOutboxHelper,
            RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher) {
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.restaurantApprovalRequestMessagePublisher = restaurantApprovalRequestMessagePublisher;
    }

    @Override
    @Transactional
    @Scheduled(
            fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<OrderApprovalOutboxMessage>> optionalMessages =
                approvalOutboxHelper.getApprovalOutboxMessagesByOutboxStatusAndSagaStatus(
                        OutboxStatus.STARTED, SagaStatus.PROCESSING);
        if (optionalMessages.isPresent() && !optionalMessages.get().isEmpty()) {
            List<OrderApprovalOutboxMessage> messages = optionalMessages.get();
            log.info(
                    "Received {} OrderApprovalOutboxMessages with ids: {}, sending to message bus.",
                    messages.size(),
                    messages.stream()
                            .map(orderApprovalOutboxMessage ->
                                    orderApprovalOutboxMessage.getId().toString())
                            .collect(Collectors.joining(",")));
            messages.forEach(orderApprovalOutboxMessage -> restaurantApprovalRequestMessagePublisher.publish(
                    orderApprovalOutboxMessage, this::updateOutboxMessageStatus));
            log.info("{} OrderApprovalOutboxMessages sent to message bus.", messages.size());
        }
    }

    // todo несовсем понятно зачем городить BiConsumer, если можно просто из паблишера вызвать application-service и
    // обновить статус. Тогда не нужна и транзакция на processOutboxMessage
    private void updateOutboxMessageStatus(
            OrderApprovalOutboxMessage orderApprovalOutboxMessage, OutboxStatus outboxStatus) {
        orderApprovalOutboxMessage.setOutboxStatus(outboxStatus);
        approvalOutboxHelper.save(orderApprovalOutboxMessage);
        log.info("OrderApprovalOutboxMessage is updated with outbox status: {}", outboxStatus);
    }
}
