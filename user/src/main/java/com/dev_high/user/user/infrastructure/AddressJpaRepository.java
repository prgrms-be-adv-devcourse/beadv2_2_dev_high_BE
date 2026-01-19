package com.dev_high.user.user.infrastructure;

import com.dev_high.user.user.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AddressJpaRepository extends JpaRepository<Address, String> {
    Optional<Address> findByUserIdAndIsDefault(String userId, boolean b);
    Optional<Address> findFirstByUserIdAndIsDefaultFalseOrderByUpdatedAtDesc(String userId);
    List<Address> findByUserIdOrderByCreatedAtDesc(String userId);
}
