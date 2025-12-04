package com.dev_high.auction;

import com.dev_high.auction.domain.Auction;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auctions")
@RestController
@RequiredArgsConstructor
public class TestController {

  private  final TestRepository  testRepository;

  @GetMapping
  public Auction test() {

    Auction auction =new Auction("TEST",100L, LocalDateTime.now(),LocalDateTime.now(),"TEST");

    return testRepository.save(auction);
  }


}
