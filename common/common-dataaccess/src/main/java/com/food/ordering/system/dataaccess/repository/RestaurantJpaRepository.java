package com.food.ordering.system.dataaccess.repository;

import com.food.ordering.system.dataaccess.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.entity.RestaurantEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// todo тоже бред, общий репозиторий для разных сервисов
@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, RestaurantEntityId> {

    Optional<List<RestaurantEntity>> findByRestaurantIdAndProductIdIn(UUID restaurantId, List<UUID> productIds);
}
