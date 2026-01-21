package com.dev_high.user.user.infrastructure;

import com.dev_high.user.user.domain.Address;
import com.dev_high.user.user.domain.AddressRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
class AddressRepositoryAdapter implements AddressRepository {
    private final AddressJpaRepository addressJpaRepository;

    public AddressRepositoryAdapter(AddressJpaRepository addressJpaRepository) {
        this.addressJpaRepository = addressJpaRepository;
    }

    @Override
    public Address save(Address address) {
        return addressJpaRepository.save(address);
    }

    @Override
    public Optional<Address> findById(String addressId) {
        return addressJpaRepository.findById(addressId);
    }

    @Override
    public Optional<Address> findCurrentDefaultAddress(String userId) {
        return addressJpaRepository.findByUserIdAndIsDefault(userId, true);
    }

    @Override
    public Optional<Address> findFirstByUserIdAndIsDefaultFalseOrderByUpdatedAtDesc(String userId) {
        return addressJpaRepository.findFirstByUserIdAndIsDefaultFalseOrderByUpdatedAtDesc(userId);
    }

    @Override
    public void delete(Address address) {
        addressJpaRepository.delete(address);
    }

    @Override
    public List<Address> findByUserIdOrderByCreatedAtDesc(String userId) {
        return addressJpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
