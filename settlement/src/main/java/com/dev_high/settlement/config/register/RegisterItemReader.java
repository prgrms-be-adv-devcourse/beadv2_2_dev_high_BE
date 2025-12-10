package com.dev_high.settlement.config.register;

import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.presentation.dto.SettlementRegisterRequest;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Iterator;
import java.util.List;

@Component
public class RegisterItemReader extends AbstractItemCountingItemStreamItemReader<Settlement> {

    private final RestTemplate restTemplate;
    private final String url = "http://localhost:8084/order/findConfirmed";
    private final int pageSize;

    private int currentPage = 0;
    private Iterator<Settlement> itemIterator;
    private boolean finished = false;

    // 생성자 주입
    public RegisterItemReader(@Value("${batch.page-size:200}") int pageSize) {
        this.restTemplate = new RestTemplate();
        this.pageSize = pageSize;
        setName("RegisterItemReader");
    }

    // ... (doOpen() 메서드는 수정 없이 유지) ...
    @Override
    protected void doOpen() throws Exception {
        if (getCurrentItemCount() > 0) {
            this.currentPage = getCurrentItemCount() / pageSize;
        }
        this.finished = false;
        readPage();
    }

    // ItemReader의 핵심 로직: SettlementRegisterRequest 타입으로 doRead() 오버라이드
    @Override
    protected Settlement doRead() throws Exception {
        if (finished) return null;

        if (itemIterator != null && itemIterator.hasNext()) {
            return itemIterator.next();
        } else if (!finished) {
            currentPage++;
            readPage();

            if (itemIterator != null && itemIterator.hasNext()) {
                return itemIterator.next();
            }
        }
        return null;
    }

    /**
     * REST API를 호출하여 한 페이지의 데이터를 가져오는 내부 메서드
     * ParameterizedTypeReference를 List<SettlementRegisterRequest> 타입으로 수정
     */
    private void readPage() {
        String requestUrl = UriComponentsBuilder.fromUriString(url)
                .queryParam("page", currentPage)  // 페이지 번호
                .queryParam("size", pageSize)     // 페이지 크기
                .toUriString();

        ResponseEntity<List<SettlementRegisterRequest>> response =
                restTemplate.exchange(
                        requestUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        }
                );

        System.out.println("response = " + response.getBody().toString());
        List<SettlementRegisterRequest> data = response.getBody();
        List<Settlement> items = data.stream().map(Settlement::fromRequest).toList();

        if (items == null || items.isEmpty()) {
            finished = true;
            itemIterator = null;
        } else {
            itemIterator = items.iterator();
        }
    }

    // ... (doClose() 메서드는 수정 없이 유지) ...
    @Override
    protected void doClose() throws Exception {
        itemIterator = null;
        finished = true;
    }

}