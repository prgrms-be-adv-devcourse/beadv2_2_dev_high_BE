package com.dev_high.settlement.config.register;

import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.presentation.dto.SettlementRegisterRequest;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 정산 데이터 주문으로부터 수집
 */
@Slf4j
@Component
public class RegisterItemReader extends AbstractItemCountingItemStreamItemReader<Settlement> {

    private final RestTemplate restTemplate;
    private final String url = "http://localhost:8084/order/findConfirmed";
    private final int pageSize;

    private int currentPage = 0;
    private Iterator<Settlement> itemIterator;
    private boolean hasMoreData = true;

    public RegisterItemReader(@Value("${batch.page-size:200}") int pageSize) {
        this.restTemplate = new RestTemplate();
        this.pageSize = pageSize;
        setName("RegisterItemReader");
    }

    @Override
    protected void doOpen() {
        log.info("RegisterItemReader 시작");
        /* 현재 읽은 아이템 수로 페이지 번호 복구 */
        if (getCurrentItemCount() > 0) this.currentPage = getCurrentItemCount() / pageSize;

        this.hasMoreData = true;
        readPage();
    }

    @Override
    protected Settlement doRead() {
        /* 1. 현재 페이지의 데이터를 다 읽었는지 확인 */
        if (itemIterator == null || !itemIterator.hasNext()) {

            /* 2. 더 읽을 페이지가 있으면 다음 페이지를 로드 */
            if (hasMoreData) {
                currentPage++;
                readPage();
            } else {
                /* 3. 더 이상 읽을 데이터가 없으면 종료 */
                log.info("RegisterItemReader 완료 - 모든 데이터 읽기 완료");
                return null;
            }
        }

        /* 4. 로드 후에도 데이터가 없으면 null 반환 (readPage에서 hasMoreData가 false로 설정됨) */
        if (itemIterator == null || !itemIterator.hasNext()) return null;

        /* 5. 데이터 반환 */
        Settlement settlement = itemIterator.next();
        log.debug("Settlement 읽음 - OrderID: {}", settlement.getOrderId());
        return settlement;
    }

    /**
     * REST API를 호출하여 한 페이지의 데이터를 가져오는 내부 메서드
     */
    private void readPage() {
        try {
            String requestUrl = UriComponentsBuilder.fromUriString(url)
                    .queryParam("page", currentPage)
                    .queryParam("size", pageSize)
                    .toUriString();

            log.info("API 호출 - Page: {}, Size: {}", currentPage, pageSize);

            ResponseEntity<List<SettlementRegisterRequest>> response =
                    restTemplate.exchange(
                            requestUrl,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {}
                    );

            List<SettlementRegisterRequest> data = response.getBody();

            if (data == null || data.isEmpty()) {
                log.info("Page {} - 데이터 없음, Reader 종료", currentPage);
                hasMoreData = false;
                itemIterator = null;
                return;
            }

            log.info("Page {} - {} 건 조회 성공", currentPage, data.size());

            List<Settlement> items = data.stream()
                    .map(Settlement::fromRequest)
                    .toList();

            /* 조회된 데이터가 pageSize(한 페이지 양)보다 적으면 마지막 페이지 */
            if (items.size() < pageSize) {
                log.info("마지막 페이지 도달 - Page: {}, Size: {}", currentPage, items.size());
                hasMoreData = false;
            }

            itemIterator = items.iterator();

        } catch (Exception e) {
            log.error("API 호출 실패 - Page: {}, Error: {}", currentPage, e.getMessage(), e);
            hasMoreData = false;
            itemIterator = null;
            throw new RuntimeException("정산 데이터 조회 실패", e);
        }
    }

    @Override
    protected void doClose() {
        log.info("RegisterItemReader 종료 - 최종 처리 페이지 번호: {}", currentPage);
        itemIterator = null;
        hasMoreData = false;
    }
}