package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OAuthProviderNotSupportedException  extends CustomException {
    public OAuthProviderNotSupportedException(String provider) {
        super(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuthProvider입니다: " + provider);
    }
}
