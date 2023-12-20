package com.food.ordering.system.restaurant.service.domain;

import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.port.output.message.publisher.OrderApprovedMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.port.output.message.publisher.OrderRejectedMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.port.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.domain.port.output.repository.RestaurantRepository;
import com.ood.ordering.system.restaurant.service.domain.RestaurantDomainService;
import com.ood.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.ood.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.ood.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RestaurantApprovalRequestHelper {

    private final RestaurantDomainService restaurantDomainService;
    private final RestaurantDataMapper restaurantDataMapper;
    private final RestaurantRepository restaurantRepository;
    private final OrderApprovalRepository orderApprovalRepository;
    private final OrderApprovedMessagePublisher orderApprovedMessagePublisher;
    private final OrderRejectedMessagePublisher orderRejectedMessagePublisher;

    public RestaurantApprovalRequestHelper(
            RestaurantDomainService restaurantDomainService,
            RestaurantDataMapper restaurantDataMapper,
            RestaurantRepository restaurantRepository,
            OrderApprovalRepository orderApprovalRepository,
            OrderApprovedMessagePublisher orderApprovedMessagePublisher,
            OrderRejectedMessagePublisher orderRejectedMessagePublisher) {
        this.restaurantDomainService = restaurantDomainService;
        this.restaurantDataMapper = restaurantDataMapper;
        this.restaurantRepository = restaurantRepository;
        this.orderApprovalRepository = orderApprovalRepository;
        this.orderApprovedMessagePublisher = orderApprovedMessagePublisher;
        this.orderRejectedMessagePublisher = orderRejectedMessagePublisher;
    }

    @Transactional
    public OrderApprovalEvent persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        log.info("Processing restaurant approval for order id: {}", restaurantApprovalRequest.getOrderId());
        List<String> failureMessages = new ArrayList<>();
        Restaurant restaurant = findRestaurant(restaurantApprovalRequest);
        OrderApprovalEvent orderApprovalEvent = restaurantDomainService.validateOrder(
                restaurant, failureMessages, orderApprovedMessagePublisher, orderRejectedMessagePublisher);
        orderApprovalRepository.save(restaurant.getOrderApproval());
        return orderApprovalEvent;
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
