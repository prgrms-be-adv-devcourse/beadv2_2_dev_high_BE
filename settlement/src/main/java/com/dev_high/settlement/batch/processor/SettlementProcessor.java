package com.dev_high.settlement.batch.processor;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.util.HttpUtil;
import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SettlementProcessor implements ItemProcessor<Settlement, Settlement> {

    private final List<Settlement> successSettlements;
    private final List<Settlement> failedSettlements;
    private final RestTemplate restTemplate;

    private void callSettlement(Settlement settlement) {

        Map<String, Object> map = new HashMap<>();
        map.put("userId", settlement.getSellerId());
        map.put("type", "CHARGE");
        map.put("depositOrderId", settlement.getId());
        map.put("amount", settlement.getFinalAmount());

        HttpEntity<Map<String, Object>> entity = HttpUtil.createGatewayEntity(map);

        String url = "http://APIGATEWAY/api/v1/deposit/usages";

        ResponseEntity<ApiResponseDto<?>> response;
        response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponseDto<?>>() {
                }
        );

        if (response.getBody() == null) {
            throw new RuntimeException("Deposit API response is null");
        }
        log.info("settle success >>>{}", response.getBody().getData());
    }

    @Override
    public Settlement process(Settlement settlement) {

        settlement.ready();

        try {

            // TODO: 외부 결제/예치금 API 호출 결과

            // 실패 TEST
            callSettlement(settlement);

            settlement.updateStatus(SettlementStatus.COMPLETED);
            successSettlements.add(settlement);

        } catch (Exception e) {
            log.error("Settlement failed for id={}", settlement.getId(), e);
            settlement.setHistoryMessage(e.getMessage());
            settlement.updateStatus(SettlementStatus.FAILED);
            failedSettlements.add(settlement);
        }
        return settlement;
    }
}

