package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.port.output.message.publisher.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class OrderApprovalEventKafkaPublisher implements RestaurantApprovalRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
    private final KafkaMessageHelper kafkaMessageHelper;

    public OrderApprovalEventKafkaPublisher(
            OrderMessagingDataMapper orderMessagingDataMapper,
            OrderServiceConfigData orderServiceConfigData,
            KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer,
            KafkaMessageHelper kafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(
            OrderApprovalOutboxMessage orderApprovalOutboxMessage,
            BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback) {
        OrderApprovalEventPayload orderApprovalEventPayload = kafkaMessageHelper.getOrderEventPayload(
                orderApprovalOutboxMessage.getPayload(), OrderApprovalEventPayload.class);
        String sagaId = orderApprovalOutboxMessage.getSagaId().toString();
        log.info(
                "Received OrderApprovalOutboxMessage for order id: {} and saga id: {}",
                orderApprovalEventPayload.getOrderId(),
                sagaId);
        try {
            RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel =
                    orderMessagingDataMapper.orderApprovalEventToRestaurantApprovalRequestAvroModel(
                            sagaId, orderApprovalEventPayload);
            kafkaProducer.send(
                    orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                    sagaId,
                    restaurantApprovalRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                            restaurantApprovalRequestAvroModel,
                            orderApprovalOutboxMessage,
                            outboxCallback,
                            orderApprovalEventPayload.getOrderId(),
                            "RestaurantApprovalRequestAvroModel"));
            log.info(
                    "OrderApprovalEventPayload sent to Kafka for order id: {} and saga id: {}",
                    orderApprovalEventPayload.getOrderId(),
                    sagaId);
        } catch (Exception exception) {
            log.error(
                    "Error while sending OrderApprovalEventPayload message to Kafka with order id: {} and saga id: {}, error: {}",
                    orderApprovalEventPayload.getOrderId(),
                    sagaId,
                    exception.getMessage());
        }
    }
}
