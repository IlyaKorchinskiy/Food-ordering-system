package com.food.ordering.system.restaurant.service.messaging.kafka.listener;

import com.food.ordering.system.kafka.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.port.input.message.listener.RestaurantApprovalRequestMessageListener;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RestaurantApprovalRequestKafkaListener implements KafkaConsumer<RestaurantApprovalRequestAvroModel> {

    private final RestaurantApprovalRequestMessageListener restaurantApprovalRequestMessageListener;
    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;

    public RestaurantApprovalRequestKafkaListener(
            RestaurantApprovalRequestMessageListener restaurantApprovalRequestMessageListener,
            RestaurantMessagingDataMapper restaurantMessagingDataMapper) {
        this.restaurantApprovalRequestMessageListener = restaurantApprovalRequestMessageListener;
        this.restaurantMessagingDataMapper = restaurantMessagingDataMapper;
    }

    @Override
    @KafkaListener(
            id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
            topics = "${restaurant-service.restaurant-approval-request-topic-name}")
    public void receive(
            @Payload List<RestaurantApprovalRequestAvroModel> messages,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
            @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info(
                "{} number of order approval requests received with keys: {}, partitions: {}, offsets: {}, sending for restaurant approval.",
                messages.size(),
                keys,
                partitions,
                offsets);
        messages.forEach(restaurantApprovalRequestAvroModel -> {
            log.info("Processing order approval for order id: {}", restaurantApprovalRequestAvroModel.getOrderId());
            RestaurantApprovalRequest restaurantApprovalRequest =
                    restaurantMessagingDataMapper.restaurantApprovalRequestAvroModelToRestaurantApprovalRequest(
                            restaurantApprovalRequestAvroModel);
            restaurantApprovalRequestMessageListener.approveOrder(restaurantApprovalRequest);
        });
    }
}
