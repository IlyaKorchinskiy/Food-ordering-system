package com.food.ordering.system.payment.service.domain.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OrderEventPayload {
    private String paymentId;
    private String customerId;
    private String orderId;
    private BigDecimal price;
    private ZonedDateTime createdAt;
    private String paymentStatus;
    private List<String> failureMessages;
}
