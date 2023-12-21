package com.food.ordering.system.restaurant.service.domain.port.output.repository;

import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;

import java.util.Optional;

public interface RestaurantRepository {

    // todo странный метод, искать ресторан по ресторану
    Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);
}
