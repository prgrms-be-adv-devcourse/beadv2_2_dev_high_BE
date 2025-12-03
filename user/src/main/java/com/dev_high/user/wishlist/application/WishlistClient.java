package com.dev_high.user.wishlist.application;

import com.dev_high.common.dto.client.product.WishlistProductResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistClient {

    private final RestTemplate restTemplate;
    private static final String GATEWAY_URL = "http://APIGATEWAY/api/v1";

    public JsonNode fetchProductInfo(String productId) {
        String url = GATEWAY_URL + "/products/" + productId;

        ResponseEntity<JsonNode> response =
                restTemplate.getForEntity(url, JsonNode.class);

        return response.getBody();
    }

    public List<WishlistProductResponse> fetchProductInfos(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        String url = GATEWAY_URL + "/products/internal";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<String>> request = new HttpEntity<>(productIds, headers);

        ResponseEntity<WishlistProductResponse[]> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        WishlistProductResponse[].class
                );

        WishlistProductResponse[] body = response.getBody();

        return body != null ? Arrays.asList(body) : List.of();
    }
}
