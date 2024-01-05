package com.food.ordering.system.customer.service.domain;

import com.food.ordering.system.customer.service.domain.create.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.create.CreateCustomerResponse;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.customer.service.domain.mapper.CustomerDataMapper;
import com.food.ordering.system.customer.service.domain.ports.input.service.CustomerApplicationService;
import com.food.ordering.system.customer.service.domain.ports.output.message.publisher.CustomerMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
public class CustomerApplicationServiceImpl implements CustomerApplicationService {

    private final CustomerDataMapper customerDataMapper;
    private final CustomerCreateCommandHandler customerCreateCommandHandler;
    private final CustomerMessagePublisher customerMessagePublisher;

    public CustomerApplicationServiceImpl(
            CustomerDataMapper customerDataMapper,
            CustomerCreateCommandHandler customerCreateCommandHandler,
            CustomerMessagePublisher customerMessagePublisher) {
        this.customerDataMapper = customerDataMapper;
        this.customerCreateCommandHandler = customerCreateCommandHandler;
        this.customerMessagePublisher = customerMessagePublisher;
    }

    @Override
    public CreateCustomerResponse createCustomer(CreateCustomerCommand createCustomerCommand) {
        CustomerCreatedEvent customerCreatedEvent = customerCreateCommandHandler.createCustomer(createCustomerCommand);
        customerMessagePublisher.publish(customerCreatedEvent);
        return customerDataMapper.customerToCreateCustomerResponse(
                customerCreatedEvent.getCustomer(), "Customer created successfully.");
    }
}
