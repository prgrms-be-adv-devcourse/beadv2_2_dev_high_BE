package com.dev_high.user.user.presentation.dto;

import com.dev_high.user.user.application.dto.UserAddressCommand;

public record UserAddressRequest(
        String zipcode,
        String state,
        String city,
        String detail,
        Boolean isDefault
) {
    public UserAddressCommand toCommand() {
        return new UserAddressCommand(zipcode, state, city, detail, isDefault);
    }

}
