package com.food.ordering.system.restaurant.service.domain;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.restaurant.service.domain.port.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.port.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.domain.port.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class RestaurantApprovalRequestHelper {

    private final RestaurantDomainService restaurantDomainService;
    private final RestaurantDataMapper restaurantDataMapper;
    private final RestaurantRepository restaurantRepository;
    private final OrderApprovalRepository orderApprovalRepository;
    private final OrderOutboxHelper orderOutboxHelper;
    private final RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher;

    public RestaurantApprovalRequestHelper(
            RestaurantDomainService restaurantDomainService,
            RestaurantDataMapper restaurantDataMapper,
            RestaurantRepository restaurantRepository,
            OrderApprovalRepository orderApprovalRepository,
            OrderOutboxHelper orderOutboxHelper,
            RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher) {
        this.restaurantDomainService = restaurantDomainService;
        this.restaurantDataMapper = restaurantDataMapper;
        this.restaurantRepository = restaurantRepository;
        this.orderApprovalRepository = orderApprovalRepository;
        this.orderOutboxHelper = orderOutboxHelper;
        this.restaurantApprovalResponseMessagePublisher = restaurantApprovalResponseMessagePublisher;
    }

    @Transactional
    public void persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        if (publishIfOutboxMessageProcessedForPayment(restaurantApprovalRequest)) {
            log.info(
                    "An outbox message with saga id: {} is already saved to database.",
                    restaurantApprovalRequest.getSagaId());
            return;
        }
        log.info("Processing restaurant approval for order id: {}", restaurantApprovalRequest.getOrderId());
        List<String> failureMessages = new ArrayList<>();
        Restaurant restaurant = findRestaurant(restaurantApprovalRequest);
        OrderApprovalEvent orderApprovalEvent = restaurantDomainService.validateOrder(restaurant, failureMessages);
        orderApprovalRepository.save(restaurant.getOrderApproval());
        orderOutboxHelper.saveOrderOutboxMessage(
                restaurantDataMapper.orderApprovalEventToOrderEventPayload(orderApprovalEvent),
                orderApprovalEvent.getOrderApproval().getOrderApprovalStatus(),
                OutboxStatus.STARTED,
                UUID.fromString(restaurantApprovalRequest.getSagaId()));
    }

    // todo несовсем понятно зачем здесь паблишить, если можно обновить сообщение в таблице, чтобы скедулер обработал
    // его.
    private boolean publishIfOutboxMessageProcessedForPayment(RestaurantApprovalRequest restaurantApprovalRequest) {
        Optional<OrderOutboxMessage> optionalOrderOutboxMessage =
                orderOutboxHelper.getCompletedOrderOutboxMessageBySagaId(
                        UUID.fromString(restaurantApprovalRequest.getSagaId()));
        if (optionalOrderOutboxMessage.isPresent()) {
            restaurantApprovalResponseMessagePublisher.publish(
                    optionalOrderOutboxMessage.get(), orderOutboxHelper::updateOutboxMessage);
            return true;
        }
        return false;
    }

    // todo сюда по идее должен приходить OrderCreatedEvent и обрабатываться: искать ресторан по ид, проверять товары и
    // т.д.
    private Restaurant findRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
        Restaurant restaurant = restaurantDataMapper.restaurantApprovalRequestToRestaurant(restaurantApprovalRequest);
        Optional<Restaurant> restaurantOptional = restaurantRepository.findRestaurantInformation(restaurant);
        if (restaurantOptional.isEmpty()) {
            log.error("Restaurant with id {} not found.", restaurant.getId().getValue());
            throw new RestaurantNotFoundException(
                    "Restaurant with id " + restaurant.getId().getValue() + " not found.");
        }
        Restaurant foundRestaurant = restaurantOptional.get();
        restaurant.setActive(foundRestaurant.isActive());
        restaurant.getOrderDetail().getProducts().forEach(product -> foundRestaurant
                .getOrderDetail()
                .getProducts()
                .forEach(foundProduct -> {
                    if (foundProduct.getId().equals(product.getId())) {
                        product.updateWithConfirmedNamePriceAndAvailability(
                                foundProduct.getName(), foundProduct.getPrice(), foundProduct.isAvailable());
                    }
                }));
        return restaurant;
    }
}
