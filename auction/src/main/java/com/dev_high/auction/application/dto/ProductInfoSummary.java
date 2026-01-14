package com.dev_high.auction.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductInfoSummary(
    String id,
    String name,
    String description,
    List<CategoryInfo> categories
) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record CategoryInfo(
      String id,
      String name
  ) {
  }
}
