package com.dev_high.user.user.domain;

import java.util.List;
import java.util.Optional;

public interface AddressRepository {
    Address save(Address address);
    Optional<Address> findById(String addressId);
    Optional<Address> findCurrentDefaultAddress(String userId);
    Optional<Address> findFirstByUserIdAndIsDefaultFalseOrderByUpdatedAtDesc(String userId);
    void delete(Address address);
    List<Address> findByUserIdOrderByCreatedAtDesc(String userId);
}
