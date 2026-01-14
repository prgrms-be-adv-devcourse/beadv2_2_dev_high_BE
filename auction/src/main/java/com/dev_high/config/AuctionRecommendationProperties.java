package com.dev_high.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auction.recommendation")
public class AuctionRecommendationProperties {

  private int similarLimit = 50;
  private int winningLimit = 200;
  private int lookbackDays = 180;
  private double startBidRatio = 0.8;
  private double winningBlendWeight = 0.8;
  private double auctionBlendWeight = 0.2;
  private double timeDecayDays = 90;
  private double minSimilarity = 0.2;
  private double rangePercent = 0.15;
  private boolean aiEnabled = true;
  private long cacheTtlMinutes = 30;
  private long durationHours = 24;
  private long startDelayMinutes = 60;
  private Map<String, Double> statusWeight = new HashMap<>();
}
