package com.dev_high.auction.domain.idclass;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Getter
@NoArgsConstructor
public class AuctionParticipationId implements Serializable {
  private String userId;
  private String auctionId;

  public AuctionParticipationId(String userId, String auctionId) {
    this.userId = userId;
    this.auctionId = auctionId;
  }
}
