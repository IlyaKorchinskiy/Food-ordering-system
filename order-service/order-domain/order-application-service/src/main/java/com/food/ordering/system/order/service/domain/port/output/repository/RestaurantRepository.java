package com.food.ordering.system.order.service.domain.port.output.repository;

import com.food.ordering.system.order.service.domain.entity.Restaurant;

import java.util.Optional;

// todo очень странный репозиторий, order service смотрит в базу restaurant-service ?
public interface RestaurantRepository {

    Optional<Restaurant> findByRestaurantInformation(Restaurant restaurant);
}
