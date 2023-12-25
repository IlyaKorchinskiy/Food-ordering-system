package com.food.ordering.system.order.service.domain.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderApprovalEventProduct {
    private String id;
    private int quantity;
}
