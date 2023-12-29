package com.food.ordering.system.kafka.producer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class KafkaMessageHelper {

    private final ObjectMapper objectMapper;

    public KafkaMessageHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T, U> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(
            String topicName,
            T avroModel,
            U outboxMessage,
            BiConsumer<U, OutboxStatus> outboxCallback,
            String orderId,
            String avroModelName) {
        return new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error(
                        "Error while sending {} message: {} and outbox type: {} to topic: {}",
                        avroModelName,
                        avroModel,
                        outboxMessage.getClass().getName(),
                        topicName,
                        ex);
                outboxCallback.accept(outboxMessage, OutboxStatus.FAILED);
            }

            @Override
            public void onSuccess(SendResult<String, T> result) {
                RecordMetadata recordMetadata = result.getRecordMetadata();
                log.info(
                        "Received successful response from Kafka for order id: {}, topic: {}, partition: {}, offset: {}, timestamp: {}",
                        orderId,
                        recordMetadata.topic(),
                        recordMetadata.partition(),
                        recordMetadata.offset(),
                        recordMetadata.timestamp());
                outboxCallback.accept(outboxMessage, OutboxStatus.COMPLETED);
            }
        };
    }

    // todo маппинг должен быть в маппере
    public <T> T getOrderEventPayload(String payload, Class<T> tClass) {
        try {
            return objectMapper.readValue(payload, tClass);
        } catch (JsonProcessingException e) {
            log.error("Could not read {} object.", tClass.getName(), e);
            throw new OrderDomainException("Could not read " + tClass.getName() + " object.", e);
        }
    }
}
