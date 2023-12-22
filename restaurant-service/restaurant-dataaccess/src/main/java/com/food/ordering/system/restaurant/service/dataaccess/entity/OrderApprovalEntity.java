package com.food.ordering.system.restaurant.service.dataaccess.entity;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_approvals", schema = "restaurant")
public class OrderApprovalEntity {

    @Id
    private UUID id;

    private UUID restaurantId;
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private OrderApprovalStatus orderApprovalStatus;
}
