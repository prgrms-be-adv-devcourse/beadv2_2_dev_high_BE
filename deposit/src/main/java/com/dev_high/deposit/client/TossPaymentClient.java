package com.dev_high.deposit.client;

import com.dev_high.deposit.application.dto.DepositPaymentDto;
import com.dev_high.deposit.client.dto.TossPaymentResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TossPaymentClient {
    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final RestTemplate restTemplate;
    private final String secretKey;

    public TossPaymentClient(@Qualifier("tossRestTemplate") RestTemplate restTemplate,
                             @Value("${payment.toss.secret-key}") String secretKey) {
        this.restTemplate = restTemplate;
        this.secretKey = secretKey;
    }

    // 하단에 try-catch를 제외하고 throw로 예외를 던져서 AOP로 처리해도 된다
    public TossPaymentResponse confirm(DepositPaymentDto.ConfirmCommand command) {
        if (secretKey == null) {
            throw new IllegalStateException("Toss secret key is not configured");
        }
        //Toss에 요청할 헤더
        HttpHeaders headers = createHeaders();

        HttpEntity<DepositPaymentDto.ConfirmCommand> entity = new HttpEntity<>(command, headers);

        try {
            return restTemplate.postForObject(CONFIRM_URL, entity, TossPaymentResponse.class);
        } catch (HttpStatusCodeException ex) {
            HttpStatusCode statusCode = ex.getStatusCode();
            String responseBody = ex.getResponseBodyAsString();
            throw new IllegalStateException("Toss confirm failed (" + statusCode + "): " + responseBody, ex);
        }
    }

    /*
     * Toss에 전달하는 헤더값 설정
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();    //header 생성
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = secretKey + ":";
        // Base64 Encode 6비트 씩 잘라서 한글자를 만든다.(secretKey)
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        //Authorization header에 인코딩 값 입력.
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        return headers;
    }
}
