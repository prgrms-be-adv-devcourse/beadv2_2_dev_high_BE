package com.dev_high.user.user.application.dto;

public record UserAddressCommand(
        String zipcode,
        String state,
        String city,
        String detail,
        Boolean isDefault
) {
}
