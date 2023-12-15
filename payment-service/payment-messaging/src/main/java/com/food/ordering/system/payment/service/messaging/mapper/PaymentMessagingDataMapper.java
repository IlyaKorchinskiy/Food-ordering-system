package com.food.ordering.system.payment.service.messaging.mapper;

import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.kafka.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.avro.model.PaymentStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentMessagingDataMapper {

    public PaymentResponseAvroModel paymentCompletedEventToPaymentResponseAvroModel(
            PaymentCompletedEvent paymentCompletedEvent) {
        return PaymentResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setPaymentId(
                        paymentCompletedEvent.getPayment().getId().getValue().toString())
                .setCustomerId(paymentCompletedEvent
                        .getPayment()
                        .getCustomerId()
                        .getValue()
                        .toString())
                .setOrderId(paymentCompletedEvent
                        .getPayment()
                        .getOrderId()
                        .getValue()
                        .toString())
                .setPrice(paymentCompletedEvent.getPayment().getPrice().getAmount())
                .setPaymentStatus(PaymentStatus.valueOf(
                        paymentCompletedEvent.getPayment().getPaymentStatus().name()))
                .setCreatedAt(paymentCompletedEvent.getCreatedAt().toInstant())
                .setFailureMessages(paymentCompletedEvent.getFailureMessages())
                .build();
    }

    // todo непонятно зачни разные модели для ивентов, если все равно маппим в одну модель кафка...
    public PaymentResponseAvroModel paymentCancelledEventToPaymentResponseAvroModel(
            PaymentCancelledEvent paymentCancelledEvent) {
        return PaymentResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setPaymentId(
                        paymentCancelledEvent.getPayment().getId().getValue().toString())
                .setCustomerId(paymentCancelledEvent
                        .getPayment()
                        .getCustomerId()
                        .getValue()
                        .toString())
                .setOrderId(paymentCancelledEvent
                        .getPayment()
                        .getOrderId()
                        .getValue()
                        .toString())
                .setPrice(paymentCancelledEvent.getPayment().getPrice().getAmount())
                .setPaymentStatus(PaymentStatus.valueOf(
                        paymentCancelledEvent.getPayment().getPaymentStatus().name()))
                .setCreatedAt(paymentCancelledEvent.getCreatedAt().toInstant())
                .setFailureMessages(paymentCancelledEvent.getFailureMessages())
                .build();
    }

    public PaymentResponseAvroModel paymentFailedEventToPaymentResponseAvroModel(
            PaymentFailedEvent paymentFailedEvent) {
        return PaymentResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setPaymentId(paymentFailedEvent.getPayment().getId().getValue().toString())
                .setCustomerId(paymentFailedEvent
                        .getPayment()
                        .getCustomerId()
                        .getValue()
                        .toString())
                .setOrderId(
                        paymentFailedEvent.getPayment().getOrderId().getValue().toString())
                .setPrice(paymentFailedEvent.getPayment().getPrice().getAmount())
                .setPaymentStatus(PaymentStatus.valueOf(
                        paymentFailedEvent.getPayment().getPaymentStatus().name()))
                .setCreatedAt(paymentFailedEvent.getCreatedAt().toInstant())
                .setFailureMessages(paymentFailedEvent.getFailureMessages())
                .build();
    }

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
}
