package com.food.orderin.system.restaurant.service.dataaccess.adapter;

import com.food.orderin.system.restaurant.service.dataaccess.entity.OrderApprovalEntity;
import com.food.orderin.system.restaurant.service.dataaccess.mapper.RestaurantDataAccessMapper;
import com.food.orderin.system.restaurant.service.dataaccess.repository.OrderApprovalJpaRepository;
import com.food.ordering.system.restaurant.service.domain.port.output.repository.OrderApprovalRepository;
import com.ood.ordering.system.restaurant.service.domain.entity.OrderApproval;
import org.springframework.stereotype.Component;

@Component
public class OrderApprovalRepositoryImpl implements OrderApprovalRepository {

    private final OrderApprovalJpaRepository orderApprovalJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    public OrderApprovalRepositoryImpl(
            OrderApprovalJpaRepository orderApprovalJpaRepository,
            RestaurantDataAccessMapper restaurantDataAccessMapper) {
        this.orderApprovalJpaRepository = orderApprovalJpaRepository;
        this.restaurantDataAccessMapper = restaurantDataAccessMapper;
    }

    @Override
    public OrderApproval save(OrderApproval orderApproval) {
        OrderApprovalEntity orderApprovalEntity =
                restaurantDataAccessMapper.orderApprovalToOrderApprovalEntity(orderApproval);
        orderApprovalEntity = orderApprovalJpaRepository.save(orderApprovalEntity);
        return restaurantDataAccessMapper.orderApprovalEntityToOrderApproval(orderApprovalEntity);
    }
}
