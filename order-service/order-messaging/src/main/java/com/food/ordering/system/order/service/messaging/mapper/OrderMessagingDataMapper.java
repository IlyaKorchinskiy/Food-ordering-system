package com.food.ordering.system.order.service.messaging.mapper;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.kafka.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.avro.model.Product;
import com.food.ordering.system.kafka.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.avro.model.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentEventPayload;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderMessagingDataMapper {

    public PaymentResponse paymentResponseAvroModelToPaymentResponse(
            PaymentResponseAvroModel paymentResponseAvroModel) {
        return PaymentResponse.builder()
                .id(paymentResponseAvroModel.getId())
                .sagaId(paymentResponseAvroModel.getSagaId())
                .paymentId(paymentResponseAvroModel.getPaymentId())
                .orderId(paymentResponseAvroModel.getOrderId())
                .customerId(paymentResponseAvroModel.getCustomerId())
                .price(paymentResponseAvroModel.getPrice())
                .createdAt(paymentResponseAvroModel.getCreatedAt())
                .paymentStatus(PaymentStatus.valueOf(
                        paymentResponseAvroModel.getPaymentStatus().name()))
                .failureMessages(paymentResponseAvroModel.getFailureMessages())
                .build();
    }

    public RestaurantApprovalResponse restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(
            RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
        return RestaurantApprovalResponse.builder()
                .id(restaurantApprovalResponseAvroModel.getId())
                .sagaId(restaurantApprovalResponseAvroModel.getSagaId())
                .orderId(restaurantApprovalResponseAvroModel.getOrderId())
                .restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId())
                .orderApprovalStatus(OrderApprovalStatus.valueOf(restaurantApprovalResponseAvroModel
                        .getOrderApprovalStatus()
                        .name()))
                .createdAt(restaurantApprovalResponseAvroModel.getCreatedAt())
                .failureMessages(restaurantApprovalResponseAvroModel.getFailureMessages())
                .build();
    }

    public PaymentRequestAvroModel orderPaymentEventToPaymentRequestAvroModel(
            String sagaId, OrderPaymentEventPayload orderPaymentEventPayload) {
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(orderPaymentEventPayload.getOrderId())
                .setSagaId(sagaId)
                .setCustomerId(orderPaymentEventPayload.getCustomerId())
                .setPrice(orderPaymentEventPayload.getPrice())
                .setPaymentOrderStatus(PaymentOrderStatus.valueOf(orderPaymentEventPayload.getPaymentOrderStatus()))
                .setCreatedAt(orderPaymentEventPayload.getCreatedAt().toInstant())
                .build();
    }

    public RestaurantApprovalRequestAvroModel orderApprovalEventToRestaurantApprovalRequestAvroModel(
            String sagaId, OrderApprovalEventPayload orderApprovalEventPayload) {
        return RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(orderApprovalEventPayload.getOrderId())
                .setSagaId(sagaId)
                .setPrice(orderApprovalEventPayload.getPrice())
                .setCreatedAt(orderApprovalEventPayload.getCreatedAt().toInstant())
                .setRestaurantId(orderApprovalEventPayload.getRestaurantId())
                .setRestaurantOrderStatus(
                        RestaurantOrderStatus.valueOf(orderApprovalEventPayload.getRestaurantOrderStatus()))
                .setProducts(orderApprovalEventPayload.getProducts().stream()
                        .map(orderApprovalEventProduct -> Product.newBuilder()
                                .setId(orderApprovalEventProduct.getId())
                                .setQuantity(orderApprovalEventProduct.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
