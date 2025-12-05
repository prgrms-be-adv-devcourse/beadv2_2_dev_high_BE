package com.dev_high.auction;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.QAuction;
import com.dev_high.common.exception.CustomException;
import com.querydsl.core.QueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import javax.naming.AuthenticationException;
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

  private  final JPAQueryFactory queryFactory;

  @GetMapping
  public Auction test() {
    QAuction qAuction = new QAuction("a1");
    Auction auction =new Auction("TEST",100L, LocalDateTime.now(),LocalDateTime.now(),"TEST");
    testRepository.saveAndFlush(auction);

//    return queryFactory.select(qAuction).from(qAuction).fetchOne();



  }


}
