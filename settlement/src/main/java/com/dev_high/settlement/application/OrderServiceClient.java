package com.dev_high.settlement.application;

import com.dev_high.common.util.HttpUtil;
import com.dev_high.settlement.presentation.dto.SettlementRegisterRequest;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OrderServiceClient {

  private final RestTemplate restTemplate;
  private final String orderServiceUrl = "http://ORDER-SERVICE/api/v1/order"; // 예시

  public List<SettlementRegisterRequest> fetchConfirmedOrders(int page, int pageSize) {
    HttpEntity entity = HttpUtil.createDirectEntity(null);
    String url = String.format("%s/confirmed?page=%d&size=%d", orderServiceUrl, page, pageSize);
    ResponseEntity<SettlementRegisterRequest[]> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        entity,
        SettlementRegisterRequest[].class
    );

    SettlementRegisterRequest[] orders = response.getBody();
    return orders != null ? Arrays.asList(orders) : List.of();
  }

}
