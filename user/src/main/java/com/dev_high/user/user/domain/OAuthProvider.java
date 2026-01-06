package com.dev_high.user.user.domain;

import com.dev_high.user.auth.exception.OAuthProviderNotSupportedException;

import java.util.Arrays;

public enum OAuthProvider {
    GOOGLE,
    NAVER;

    public static OAuthProvider from(String provider) {
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(provider))
                .findFirst()
                .orElseThrow(() ->
                        new OAuthProviderNotSupportedException(provider)
                );
    }
}

