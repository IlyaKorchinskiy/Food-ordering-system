package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.port.output.message.publisher.PaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class OrderPaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final KafkaMessageHelper kafkaMessageHelper;

    public OrderPaymentEventKafkaPublisher(
            OrderMessagingDataMapper orderMessagingDataMapper,
            OrderServiceConfigData orderServiceConfigData,
            KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
            KafkaMessageHelper kafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(
            OrderPaymentOutboxMessage orderPaymentOutboxMessage,
            BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback) {
        // todo непонятно зачем конвертить строку в объект и обратно. в OrderPaymentOutboxMessage можно было оставить
        // объект.
        OrderPaymentEventPayload orderPaymentEventPayload = kafkaMessageHelper.getOrderEventPayload(
                orderPaymentOutboxMessage.getPayload(), OrderPaymentEventPayload.class);
        String sagaId = orderPaymentOutboxMessage.getSagaId().toString();
        log.info(
                "Received OrderPaymentOutboxMessage for order id: {} and saga id: {}",
                orderPaymentEventPayload.getOrderId(),
                sagaId);
        try {
            // todo на order created event отправляет payment request, выглядит некорректно
            PaymentRequestAvroModel paymentRequestAvroModel =
                    orderMessagingDataMapper.orderPaymentEventToPaymentRequestAvroModel(
                            sagaId, orderPaymentEventPayload);
            kafkaProducer.send(
                    orderServiceConfigData.getPaymentRequestTopicName(),
                    sagaId,
                    paymentRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getPaymentRequestTopicName(),
                            paymentRequestAvroModel,
                            orderPaymentOutboxMessage,
                            outboxCallback,
                            orderPaymentEventPayload.getOrderId(),
                            "PaymentRequestAvroModel"));
            log.info(
                    "OrderPaymentEventPayload sent to Kafka for order id: {} and saga id: {}",
                    orderPaymentEventPayload.getOrderId(),
                    sagaId);
        } catch (Exception exception) {
            log.error(
                    "Error while sending OrderPaymentEventPayload message to Kafka with order id: {} and saga id: {}, error: {}",
                    orderPaymentEventPayload.getOrderId(),
                    sagaId,
                    exception.getMessage());
        }
    }
}
