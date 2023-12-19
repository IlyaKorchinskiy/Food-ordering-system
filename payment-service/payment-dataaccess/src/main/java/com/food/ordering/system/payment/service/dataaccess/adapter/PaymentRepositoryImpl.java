package com.food.ordering.system.payment.service.dataaccess.adapter;

import com.food.ordering.system.payment.service.dataaccess.entity.PaymentEntity;
import com.food.ordering.system.payment.service.dataaccess.mapper.PaymentDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.repository.PaymentJpaRepository;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.port.output.repository.PaymentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentDataAccessMapper paymentDataAccessMapper;

    public PaymentRepositoryImpl(
            PaymentJpaRepository paymentJpaRepository, PaymentDataAccessMapper paymentDataAccessMapper) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.paymentDataAccessMapper = paymentDataAccessMapper;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity paymentEntity = paymentDataAccessMapper.paymentToPaymentEntity(payment);
        paymentEntity = paymentJpaRepository.save(paymentEntity);
        return paymentDataAccessMapper.paymentEntityToPayment(paymentEntity);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        Optional<PaymentEntity> optionalPaymentEntity = paymentJpaRepository.findByOrderId(orderId);
        return optionalPaymentEntity.map(paymentDataAccessMapper::paymentEntityToPayment);
    }
}
