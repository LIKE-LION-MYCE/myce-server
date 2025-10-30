package com.myce.payment.service.portone.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.payment.config.PortOneConfig;
import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.service.portone.PortOneApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneApiServiceImpl implements PortOneApiService {

    private final PortOneConfig portOneConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 액세스 토큰 발급
    @Override
    public String getAccessToken() {
        String url = "https://api.iamport.kr/users/getToken";

        log.info("[포트원 토큰 요청] imp_key={}, imp_secret={}",
                portOneConfig.getApiKey(), portOneConfig.getApiSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // TODO MultiValueMap을 사용한 이유?
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("imp_key", portOneConfig.getApiKey());
        body.add("imp_secret", portOneConfig.getApiSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, request, String.class);
            log.info("[포트원 응답 전체] {}", response.getBody());
        } catch (Exception e) {
            log.error("[포트원 토큰 발급 실패] {}", e.getMessage());
            throw new CustomException(CustomErrorCode.PORTONE_AUTHENTICATION_FAILED);
        }

        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String accessToken = rootNode.path("response").path("access_token").asText();
            if (accessToken.isEmpty()) {
                log.error("[포트원 토큰 파싱 실패] 응답에 access_token이 없습니다. 응답: {}", response.getBody());
                throw new CustomException(CustomErrorCode.PORTONE_AUTHENTICATION_FAILED);
            }
            log.info("[포트원 accessToken] accessToken={}", accessToken);
            return accessToken;
        } catch (JsonProcessingException e) {
            log.error("[포트원 토큰 파싱 실패] {}", e.getMessage());
            throw new CustomException(CustomErrorCode.PORTONE_AUTHENTICATION_FAILED);
        }
    }
    
    // 결제 내역 조회
    @Override
    public Map<String, Object> getPaymentInfo(String impUid) {
        String accessToken = getAccessToken();
        // TODO 포트원URL yml로 분리
        String url = "https://api.iamport.kr/payments/" + impUid;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = response.getBody();
        if (body == null || body.get("response") == null) {
            throw new CustomException(CustomErrorCode.PORTONE_PAYMENT_NOT_FOUND);
        }
        return (Map<String, Object>) body.get("response");
    }

    // 환불 요청
    @Override
    public Map<String, Object> requestRefundToPortOne(PaymentRefundRequest request, Integer originalPaidAmount, String accessToken) {
        String url = "https://api.iamport.kr/payments/cancel";
        Map<String, Object> body = buildRefundRequestBody(request, originalPaidAmount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
            log.info("[포트원 환불 요청 바디] {}", jsonBody);
        } catch (JsonProcessingException e) {
            throw new CustomException(CustomErrorCode.PORTONE_REQUEST_SERIALIZATION_FAILED);
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
            log.info("[포트원 환불 응답 전체] {}", response.getBody());
        } catch (Exception e) {
            log.error("[포트원 환불 API 호출 실패] {}", e.getMessage());
            throw new CustomException(CustomErrorCode.PORTONE_REFUND_FAILED);
        }

        Map<String, Object> fullResponse = response.getBody();
        Map<String, Object> responseBody = (Map<String, Object>) fullResponse.get("response");
        
        if (responseBody == null) {
            log.error("[포트원 환불 응답 오류] response 필드가 없음. 전체 응답: {}", fullResponse);
            throw new CustomException(CustomErrorCode.PORTONE_REFUND_FAILED);
        }
        
        String status = (String) responseBody.get("status");
        if (!("cancelled".equals(status) || "paid".equals(status))) {
            log.error("[포트원 환불 상태 오류] 예상: cancelled/paid, 실제: {}. 전체 응답: {}", status, responseBody);
            throw new CustomException(CustomErrorCode.PORTONE_REFUND_FAILED);
        }
        return responseBody;
    }

    // 환불 요청 바디 구성
    private Map<String, Object> buildRefundRequestBody(PaymentRefundRequest request, Integer originalPaidAmount) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (request.getImpUid() != null) {
            body.put("imp_uid", request.getImpUid());
        } else if (request.getMerchantUid() != null) {
            body.put("merchant_uid", request.getMerchantUid());
        }

        if (request.getReason() != null) {
            body.put("reason", request.getReason());
        }

        // 환불 금액 설정 (부분 환불이면 해당 금액, 전체 환불이면 원본 금액)
        Integer refundAmount = request.getCancelAmount() != null ? request.getCancelAmount() : originalPaidAmount;
        if (refundAmount > originalPaidAmount) {
            throw new CustomException(CustomErrorCode.REFUND_AMOUNT_EXCEEDS_PAID);
        }
        body.put("amount", refundAmount);
        body.put("checksum", originalPaidAmount);

        if (request.getRefundHolder() != null) {
            body.put("refund_holder", request.getRefundHolder());
        }
        if (request.getRefundBank() != null) {
            body.put("refund_bank", request.getRefundBank());
        }
        if (request.getRefundAccount() != null) {
            body.put("refund_account", request.getRefundAccount());
        }
        if (request.getRefundTel() != null) {
            body.put("refund_tel", request.getRefundTel());
        }
        return body;    }
}
