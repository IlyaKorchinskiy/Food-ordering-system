package com.food.orderin.system.restaurant.service.dataaccess.adapter;

import com.food.orderin.system.restaurant.service.dataaccess.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.dataaccess.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.repository.RestaurantJpaRepository;
import com.food.ordering.system.restaurant.service.domain.port.output.repository.RestaurantRepository;
import com.ood.ordering.system.restaurant.service.domain.entity.Restaurant;
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
    public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
        List<UUID> productIds = restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
        Optional<List<RestaurantEntity>> restaurantEntities = restaurantJpaRepository.findByRestaurantIdAndProductIdIn(
                restaurant.getId().getValue(), productIds);
        return restaurantEntities.map(restaurantDataAccessMapper::restaurantEntitiesToRestaurant);
    }
}
