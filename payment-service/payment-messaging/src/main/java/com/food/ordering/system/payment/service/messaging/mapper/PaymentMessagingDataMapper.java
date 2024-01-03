package com.food.ordering.system.payment.service.messaging.mapper;

import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.kafka.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.avro.model.PaymentStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentMessagingDataMapper {

    public PaymentRequest paymentRequestAvroModelToPaymentRequest(PaymentRequestAvroModel paymentRequestAvroModel) {
        return PaymentRequest.builder()
                .id(paymentRequestAvroModel.getId())
                .sagaId(paymentRequestAvroModel.getSagaId())
                .orderId(paymentRequestAvroModel.getOrderId())
                .customerId(paymentRequestAvroModel.getCustomerId())
                .price(paymentRequestAvroModel.getPrice())
                .paymentOrderStatus(PaymentOrderStatus.valueOf(
                        paymentRequestAvroModel.getPaymentOrderStatus().name()))
                .createdAt(paymentRequestAvroModel.getCreatedAt())
                .build();
    }

    public PaymentResponseAvroModel orderEventPayloadToPaymentResponseAvroModel(
            String sagaId, OrderEventPayload orderEventPayload) {
        return PaymentResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setPaymentId(orderEventPayload.getPaymentId())
                .setSagaId(sagaId)
                .setOrderId(orderEventPayload.getOrderId())
                .setCustomerId(orderEventPayload.getCustomerId())
                .setCreatedAt(orderEventPayload.getCreatedAt().toInstant())
                .setPrice(orderEventPayload.getPrice())
                .setPaymentStatus(PaymentStatus.valueOf(orderEventPayload.getPaymentStatus()))
                .setFailureMessages(orderEventPayload.getFailureMessages())
                .build();
    }
}
