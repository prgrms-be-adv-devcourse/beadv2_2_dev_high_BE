package com.dev_high.user.auth.application.oauth;

import com.dev_high.user.user.domain.OAuthProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class SocialOAuthServiceFactory {

    private final List<SocialOAuthService> socialOAuthServices;
    private Map<OAuthProvider, SocialOAuthService> serviceMap;

    public SocialOAuthServiceFactory(List<SocialOAuthService> socialOAuthServices) {
        this.socialOAuthServices = socialOAuthServices;
    }

    @PostConstruct
    public void init() {
        serviceMap = socialOAuthServices.stream()
                .collect(Collectors.toMap(SocialOAuthService::getProvider, Function.identity()));
    }

    public SocialOAuthService getService(OAuthProvider provider) {
        return serviceMap.get(provider);
    }
}
