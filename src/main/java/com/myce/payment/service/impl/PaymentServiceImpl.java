package com.myce.payment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.payment.config.PortOneConfig;
import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.service.PaymentService;
import com.myce.payment.service.mapper.PaymentMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
  private final PortOneConfig portOneConfig;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  // 액세스 토큰 받아오기
  @Override
  public String getAccessToken() {
    String url = "https://api.iamport.kr/users/getToken";

    log.info("[포트원 토큰 요청] imp_key={}, imp_secret={}",
        portOneConfig.getApiKey(), portOneConfig.getApiSecret());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

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

  // 결제 검증
  @Override
  public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
    // 액세스 토큰 발급
    String accessToken = getAccessToken();

    // 포트원 결제 내역 조회
    Map<String, Object> portOnePayment = getPaymentInfo(request.getImpUid(), accessToken);

    // 결제 정보 검증
    String status = (String) portOnePayment.get("status");
    Integer paidAmount = (Integer) portOnePayment.get("amount");
    String merchantUid = (String) portOnePayment.get("merchant_uid");

    // status, amount, merchantUid 검증
    if (!"paid".equalsIgnoreCase(status)) {
      throw new CustomException(CustomErrorCode.PAYMENT_NOT_PAID);
    }
    if (!paidAmount.equals(request.getAmount())) {
      throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }
    if (!merchantUid.equals(request.getMerchantUid())) {
      throw new CustomException(CustomErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
    }

    // 4. 검증 성공 시, 결제 내역 저장
    // 카드 : amount, buyer_name, card_number, imp_uid, merchant_uid, name(결제 항목), paid_at은 내가 직접, pay_method, provider는 pg_provider

    // 5. PaymentVerifyResponse 생성 및 반환
    return PaymentMapper.toPaymentVerifyResponse(request, status, paidAmount);
  }

  @Override
  public Map<String, Object> getPaymentInfo(String impUid, String accessToken) {
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
}