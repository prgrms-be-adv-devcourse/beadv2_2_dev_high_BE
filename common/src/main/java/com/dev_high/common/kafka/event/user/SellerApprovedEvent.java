package com.dev_high.common.kafka.event.user;

import java.util.List;

public record SellerApprovedEvent(
        List<String> userIds
) {
}
