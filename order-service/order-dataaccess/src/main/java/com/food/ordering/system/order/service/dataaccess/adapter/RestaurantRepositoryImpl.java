package com.food.ordering.system.order.service.dataaccess.adapter;

import com.food.ordering.system.order.service.dataaccess.entity.RestaurantEntity;
import com.food.ordering.system.order.service.dataaccess.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.repository.RestaurantJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.port.output.repository.RestaurantRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    public RestaurantRepositoryImpl(
            RestaurantJpaRepository restaurantJpaRepository, RestaurantDataAccessMapper restaurantDataAccessMapper) {
        this.restaurantJpaRepository = restaurantJpaRepository;
        this.restaurantDataAccessMapper = restaurantDataAccessMapper;
    }

    @Override
    public Optional<Restaurant> findByRestaurantInformation(Restaurant restaurant) {
        List<UUID> productUuids = restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
        Optional<List<RestaurantEntity>> restaurantEntities = restaurantJpaRepository.findByRestaurantIdAndProductIdIn(
                restaurant.getId().getValue(), productUuids);
        return restaurantEntities.map(restaurantDataAccessMapper::restaurantEntitiesToRestaurant);
    }
}
