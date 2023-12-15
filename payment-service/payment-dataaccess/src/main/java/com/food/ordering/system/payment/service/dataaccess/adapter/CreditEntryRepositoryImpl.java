package com.food.ordering.system.payment.service.dataaccess.adapter;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.dataaccess.entity.CreditEntryEntity;
import com.food.ordering.system.payment.service.dataaccess.mapper.CreditEntryDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.repository.CreditEntryJpaRepository;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CreditEntryRepositoryImpl implements CreditEntryRepository {

    private final CreditEntryJpaRepository creditEntryJpaRepository;
    private final CreditEntryDataAccessMapper creditEntryDataAccessMapper;

    public CreditEntryRepositoryImpl(
            CreditEntryJpaRepository creditEntryJpaRepository,
            CreditEntryDataAccessMapper creditEntryDataAccessMapper) {
        this.creditEntryJpaRepository = creditEntryJpaRepository;
        this.creditEntryDataAccessMapper = creditEntryDataAccessMapper;
    }

    @Override
    public CreditEntry save(CreditEntry creditEntry) {
        CreditEntryEntity creditEntryEntity = creditEntryDataAccessMapper.creditEntryToCreditEntryEntity(creditEntry);
        creditEntryEntity = creditEntryJpaRepository.save(creditEntryEntity);
        return creditEntryDataAccessMapper.creditEntryEntityToCreditEntry(creditEntryEntity);
    }

    @Override
    public Optional<CreditEntry> findByCustomerId(CustomerId customerId) {
        Optional<CreditEntryEntity> creditEntryEntity =
                creditEntryJpaRepository.findByCustomerId(customerId.getValue());
        return creditEntryEntity.map(creditEntryDataAccessMapper::creditEntryEntityToCreditEntry);
    }
}
