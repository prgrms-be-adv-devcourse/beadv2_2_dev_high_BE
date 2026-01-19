package com.dev_high.user.user.application.dto;

import com.dev_high.user.user.domain.Address;

public record UserAddressResponse(
        String id,
        String zipcode,
        String state,
        String city,
        String detail,
        Boolean isDefault
) {

    public static UserAddressResponse of(Address address) {
        return new UserAddressResponse(address.getId(), address.getZipCode(), address.getState(), address.getCity(), address.getDetail(), address.getIsDefault());
    }
}
