package com.dev_high.common.kafka.event.product;

import java.util.List;

public record ProductCreateSearchRequestEvent(String productId,
                                              String productName,
                                              List<String> categories,
                                              String description,
                                              String status,
                                              String sellerId
                                              ) {


}
