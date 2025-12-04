package com.dev_high.auction;

import com.dev_high.auction.domain.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<Auction ,String> {


}
