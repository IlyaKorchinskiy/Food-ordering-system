package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class OrderOutboxCleanerScheduler implements OutboxScheduler {

    private final OrderOutboxHelper orderOutboxHelper;

    public OrderOutboxCleanerScheduler(OrderOutboxHelper orderOutboxHelper) {
        this.orderOutboxHelper = orderOutboxHelper;
    }

    @Override
    @Scheduled(cron = "@midnight")
    @Transactional
    public void processOutboxMessage() {
        Optional<List<OrderOutboxMessage>> optionalMessages =
                orderOutboxHelper.getOrderOutboxMessagesByOutboxStatus(OutboxStatus.COMPLETED);
        if (optionalMessages.isPresent() && !optionalMessages.get().isEmpty()) {
            List<OrderOutboxMessage> messages = optionalMessages.get();
            log.info("Received {} OrderOutboxMessages to clean-up.", messages.size());
            orderOutboxHelper.deleteOrderOutboxMessagesByOutboxStatus(OutboxStatus.COMPLETED);
            log.info("{} OrderOutboxMessages deleted.", messages.size());
        }
    }
}
