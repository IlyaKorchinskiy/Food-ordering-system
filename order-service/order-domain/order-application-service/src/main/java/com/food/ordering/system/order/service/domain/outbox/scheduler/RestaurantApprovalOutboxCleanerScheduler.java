package com.food.ordering.system.order.service.domain.outbox.scheduler;

import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestaurantApprovalOutboxCleanerScheduler implements OutboxScheduler {

    private final ApprovalOutboxHelper approvalOutboxHelper;

    public RestaurantApprovalOutboxCleanerScheduler(ApprovalOutboxHelper approvalOutboxHelper) {
        this.approvalOutboxHelper = approvalOutboxHelper;
    }

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<OrderApprovalOutboxMessage>> optionalMessages =
                approvalOutboxHelper.getApprovalOutboxMessagesByOutboxStatusAndSagaStatuses(
                        OutboxStatus.COMPLETED, SagaStatus.SUCCEEDED, SagaStatus.COMPENSATED, SagaStatus.FAILED);
        if (optionalMessages.isPresent()) {
            List<OrderApprovalOutboxMessage> messages = optionalMessages.get();
            log.info(
                    "Received {} OrderApprovalOutboxMessages to clean-up. The payloads: {}",
                    messages.size(),
                    messages.stream()
                            .map(OrderApprovalOutboxMessage::getPayload)
                            .collect(Collectors.joining("\n")));
            approvalOutboxHelper.deleteApprovalOutboxMessagesByOutboxStatusAndSagaStatuses(
                    OutboxStatus.COMPLETED, SagaStatus.SUCCEEDED, SagaStatus.COMPENSATED, SagaStatus.FAILED);
            log.info("{} OrderApprovalOutboxMessages deleted.", messages.size());
        }
    }
}
