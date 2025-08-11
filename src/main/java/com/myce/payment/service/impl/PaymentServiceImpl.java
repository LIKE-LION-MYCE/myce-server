package com.myce.payment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdvertisementRepository;
import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.ExpoRepository;
import com.myce.payment.config.PortOneConfig;
import com.myce.payment.dto.PaymentInfoForRefund;
import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.PortOneWebhookRequest;
import com.myce.payment.dto.UserIdentifier;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.payment.service.PaymentService;
import com.myce.payment.service.mapper.PaymentMapper;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.system.entity.AdFeeSetting;
import com.myce.system.entity.ExpoFeeSetting;
import com.myce.system.repository.AdFeeSettingRepository;
import com.myce.system.repository.ExpoFeeSettingRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;
  private final ReservationRepository reservationRepository;
  private final AdPaymentInfoRepository adPaymentInfoRepository;
  private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
  private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
  private final AdvertisementRepository advertisementRepository;
  private final ExpoRepository expoRepository;
  private final AdFeeSettingRepository adFeeSettingRepository;
  private final ExpoFeeSettingRepository expoFeeSettingRepository;
  private final RefundRepository refundRepository;

  // 포트원 API 액세스 토큰을 발급받는 메소드
  private String getAccessToken() {
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

  // 결제 정보를 검증하고 저장하는 메소드
  @Override
  @Transactional
  public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
    String accessToken = getAccessToken();
    Map<String, Object> portOnePayment = getPaymentInfo(request.getImpUid(), accessToken);
    Integer paidAmount = verifyPaymentDetails(request, portOnePayment);
    identifyUser(request);
    Payment payment = paymentMapper.toEntity(request, portOnePayment);
    paymentRepository.save(payment);
    Object paymentInfo = savePaymentInfoDetails(request, paidAmount, payment, PaymentStatus.SUCCESS);
    return paymentMapper.toPaymentVerifyResponse(payment, paymentInfo);
  }

  // 결제를 환불하는 메소드
  @Override
  @Transactional
  public Map<String, Object> refundPayment(PaymentRefundRequest request) {
    log.info("[포트원 환불 impUid] impUid={}", request.getImpUid());
    String accessToken = getAccessToken();
    Payment payment = paymentRepository.findByImpUid(request.getImpUid())
        .orElseGet(() -> paymentRepository.findByMerchantUid(request.getMerchantUid())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND)));

    PaymentInfoForRefund paymentInfoForRefund = getPaymentInfoForRefund(payment);
    Object paymentInfoEntity = paymentInfoForRefund.getPaymentInfoEntity();
    Integer originalPaidAmount = paymentInfoForRefund.getOriginalPaidAmount();

    Map<String, Object> responseBody = requestRefundToPortOne(request, originalPaidAmount,
        accessToken);

    processRefund(request, payment, paymentInfoEntity, originalPaidAmount, responseBody);

    return responseBody;
  }

  // 가상계좌 검증
  @Override
  @Transactional
  public PaymentVerifyResponse verifyVbankPayment(PaymentVerifyRequest request) {
    // 액세스 토큰 받기
    String accessToken = getAccessToken();

    // 포트원을 통해 결제내역 받아옴
    Map<String, Object> portOnePayment = getPaymentInfo(request.getImpUid(), accessToken);
    Integer paidAmount = verifyVbankDetails(request, portOnePayment);

    identifyUser(request);

    // 가상계좌 결제는 PENDING 상태로 저장
    Payment payment = paymentMapper.toEntity(request, portOnePayment);
    paymentRepository.save(payment);

    // PaymentInfoDetails 저장 (PENDING 상태로)
    Object paymentInfo = savePaymentInfoDetails(request, paidAmount, payment, PaymentStatus.PENDING);

    return paymentMapper.toPaymentVerifyResponse(payment, paymentInfo);
  }

  @Override
  @Transactional
  public void processWebhook(PortOneWebhookRequest request) {
    // 내역 조회 후 저장
    String accessToken = getAccessToken();
    Map<String, Object> pay =  getPaymentInfo(request.getImpUid(), accessToken);

    // 포트원 재조회 결과에서 핵심 값 추출
    String status = (String) pay.get("status");
    String portOneMerchantUid = (String) pay.get("merchant_uid");
    Integer paidAmount = ((Number) pay.getOrDefault("amount", 0)).intValue();

    // 입금완료 아니면 무시
    if (!"paid".equalsIgnoreCase(status)) {
      log.info("[웹훅 무시] 포트원 상태가 paid 아님. status={}", status);
      return;
    }

    // 결제 시점 추출
    Long paidAt = ((Number) pay.getOrDefault("paid_at", 0)).longValue();

    // 우리 Payment 조회 (imp_uid 우선, 없으면 merchant_uid)
    Payment payment = paymentRepository.findByImpUid(request.getImpUid())
        .orElseGet(() -> paymentRepository.findByMerchantUid(request.getMerchantUid())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND)));

    // 타깃별 PaymentInfo 조회 -> 이미 조회했는지 체크 -> 금액 검증 -> SUCCESS
    switch (payment.getTargetType()) {
      case RESERVATION -> {
        ReservationPaymentInfo rpi = reservationPaymentInfoRepository
            .findByReservationId(payment.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        // 이미 조회했는지 체크 (웹훅이 하나만 가는 게 아니므로 이미 체크한 건은 안 해도 됨)
        if (rpi.getStatus() == PaymentStatus.SUCCESS
            || rpi.getStatus() == PaymentStatus.REFUNDED
            || rpi.getStatus() == PaymentStatus.PARTIAL_REFUNDED) {
          return;
        }

        // 금액 검증
        if (!Integer.valueOf(rpi.getTotalAmount()).equals(paidAmount)) {
          throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // SUCCESS로 변환
        rpi.setStatus(PaymentStatus.SUCCESS);
        reservationPaymentInfoRepository.save(rpi);
      }

      case AD -> {
        AdPaymentInfo api = adPaymentInfoRepository
            .findByAdvertisementId(payment.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        if (api.getStatus() == PaymentStatus.SUCCESS
            || api.getStatus() == PaymentStatus.REFUNDED
            || api.getStatus() == PaymentStatus.PARTIAL_REFUNDED) {
          return;
        }

        if (!Integer.valueOf(api.getTotalAmount()).equals(paidAmount)) {
          throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        api.setStatus(PaymentStatus.SUCCESS);
        adPaymentInfoRepository.save(api);
      }

      case EXPO -> {
        ExpoPaymentInfo epi = expoPaymentInfoRepository
            .findByExpoId(payment.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        if (epi.getStatus() == PaymentStatus.SUCCESS
            || epi.getStatus() == PaymentStatus.REFUNDED
            || epi.getStatus() == PaymentStatus.PARTIAL_REFUNDED) {
          return;
        }

        if (!Integer.valueOf(epi.getTotalAmount()).equals(paidAmount)) {
          throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        epi.setStatus(PaymentStatus.SUCCESS);
        expoPaymentInfoRepository.save(epi);
      }

      default -> throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
    }
    // 7) Payment 결제 시점 업데이트
    payment.updateOnSuccess(
        LocalDateTime.ofEpochSecond(paidAt, 0, ZoneOffset.ofHours(9))
    );
    paymentRepository.save(payment);

    log.info("[웹훅 처리 완료] imp_uid={}, merchant_uid={}, paid_at={}",
        request.getImpUid(), portOneMerchantUid, paidAt);
  }

  private Integer verifyVbankDetails(PaymentVerifyRequest request, Map<String, Object> portOnePayment) {
    String status = (String) portOnePayment.get("status");
    Integer paidAmount = (Integer) portOnePayment.get("amount");
    String merchantUid = (String) portOnePayment.get("merchant_uid");

    // 가상계좌는 ready 상태로 넘어오므로, ready 상태도 유효한 것으로 간주
    if (!"ready".equalsIgnoreCase(status) && !"paid".equalsIgnoreCase(status)) {
      log.error("[가상계좌 검증 실패] 포트원 결제 상태가 'ready' 또는 'paid'가 아님: {}", status);
      throw new CustomException(CustomErrorCode.PAYMENT_NOT_READY_OR_PAID);
    }
    if (!paidAmount.equals(request.getAmount())) {
      log.error("[가상계좌 검증 실패] 결제 금액 불일치. 요청 금액: {}, 실제 금액: {}", request.getAmount(), paidAmount);
      throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }
    if (!merchantUid.equals(request.getMerchantUid())) {
      log.error("[가상계좌 검증 실패] 상점 UID 불일치. 요청 UID: {}, 실제 UID: {}", request.getMerchantUid(), merchantUid);
      throw new CustomException(CustomErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
    }
    return paidAmount;
  }

  // 환불 처리 후 정보를 저장하는 메소드
  private void processRefund(PaymentRefundRequest request, Payment payment, Object paymentInfoEntity,
      Integer originalPaidAmount, Map<String, Object> responseBody) {
    Integer refundedAmount = (Integer) responseBody.get("cancel_amount");
    boolean isPartial =
        request.getCancelAmount() != null && request.getCancelAmount() < originalPaidAmount;

    Refund refund = Refund.builder()
        .payment(payment)
        .amount(refundedAmount)
        .reason(request.getReason())
        .status(RefundStatus.REFUNDED)
        .isPartial(isPartial)
        .refundedAt(LocalDateTime.now())
        .build();
    refundRepository.save(refund);

    updatePaymentInfoStatus(paymentInfoEntity, isPartial);
  }

  // 결제 정보 상태를 업데이트하는 메소드
  private void updatePaymentInfoStatus(Object paymentInfoEntity, boolean isPartial) {
    if (paymentInfoEntity instanceof ReservationPaymentInfo) {
      ReservationPaymentInfo rpi = (ReservationPaymentInfo) paymentInfoEntity;
      rpi.setStatus(isPartial ? PaymentStatus.PARTIAL_REFUNDED : PaymentStatus.REFUNDED);
      reservationPaymentInfoRepository.save(rpi);
    } else if (paymentInfoEntity instanceof AdPaymentInfo) {
      AdPaymentInfo api = (AdPaymentInfo) paymentInfoEntity;
      api.setStatus(isPartial ? PaymentStatus.PARTIAL_REFUNDED : PaymentStatus.REFUNDED);
      adPaymentInfoRepository.save(api);
    } else if (paymentInfoEntity instanceof ExpoPaymentInfo) {
      ExpoPaymentInfo epi = (ExpoPaymentInfo) paymentInfoEntity;
      epi.setStatus(isPartial ? PaymentStatus.PARTIAL_REFUNDED : PaymentStatus.REFUNDED);
      expoPaymentInfoRepository.save(epi);
    }
  }

  // 포트원 API에 환불을 요청하는 메소드
  private Map<String, Object> requestRefundToPortOne(PaymentRefundRequest request,
      Integer originalPaidAmount, String accessToken) {
    String url = "https://api.iamport.kr/payments/cancel";
    Map<String, Object> body = buildRefundRequestBody(request, originalPaidAmount);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(accessToken);

    String jsonBody;
    try {
      jsonBody = objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      throw new CustomException(CustomErrorCode.PORTONE_REQUEST_SERIALIZATION_FAILED);
    }

    HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

    ResponseEntity<Map> response;
    try {
      response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
    } catch (Exception e) {
      throw new CustomException(CustomErrorCode.PORTONE_REFUND_FAILED);
    }

    Map<String, Object> responseBody = (Map<String, Object>) response.getBody().get("response");
    if (responseBody == null || !("cancelled".equals(responseBody.get("status"))
        || "paid".equals(responseBody.get("status")))) {
      throw new CustomException(CustomErrorCode.PORTONE_REFUND_FAILED);
    }
    return responseBody;
  }

  // 포트원 API 환불 요청 본문을 생성하는 메소드
  private Map<String, Object> buildRefundRequestBody(PaymentRefundRequest request,
      Integer originalPaidAmount) {
    Map<String, Object> body = new LinkedHashMap<>();
    if (request.getImpUid() != null) {
      body.put("imp_uid", request.getImpUid());
    } else if (request.getMerchantUid() != null) {
      body.put("merchant_uid", request.getMerchantUid());
    }

    if (request.getReason() != null) {
      body.put("reason", request.getReason());
    }

    if (request.getCancelAmount() != null) {
      if (request.getCancelAmount() > originalPaidAmount) {
        throw new CustomException(CustomErrorCode.REFUND_AMOUNT_EXCEEDS_PAID);
      }
      body.put("amount", request.getCancelAmount());
    }
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
    return body;
  }

  // 환불할 결제 정보를 조회하는 메소드
  private PaymentInfoForRefund getPaymentInfoForRefund(Payment payment) {
    Object paymentInfoEntity;
    Integer originalPaidAmount;

    switch (payment.getTargetType()) {
      case RESERVATION:
        ReservationPaymentInfo rpi = reservationPaymentInfoRepository.findByReservationId(
                payment.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        paymentInfoEntity = rpi;
        originalPaidAmount = rpi.getTotalAmount();
        break;
      case AD:
        AdPaymentInfo api = adPaymentInfoRepository.findByAdvertisementId(payment.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        paymentInfoEntity = api;
        originalPaidAmount = api.getTotalAmount();
        break;
      case EXPO:
        ExpoPaymentInfo epi = expoPaymentInfoRepository.findByExpoId(payment.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        paymentInfoEntity = epi;
        originalPaidAmount = epi.getTotalAmount();
        break;
      default:
        throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
    }
    return new PaymentInfoForRefund(paymentInfoEntity, originalPaidAmount);
  }

  // 결제 주체를 식별하는 메소드
  private UserIdentifier identifyUser(PaymentVerifyRequest request) {
    UserType userType = null;
    Long userId = null;

    switch (request.getTargetType()) {
      case RESERVATION:
        Reservation reservation = reservationRepository.findById(request.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        userType = reservation.getUserType();
        userId = reservation.getUserId();
        break;
      case AD:
      case EXPO:
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken) {
          throw new CustomException(CustomErrorCode.REFRESH_TOKEN_NOT_EXIST);
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        userType = UserType.MEMBER;
        userId = userDetails.getMemberId();
        break;
      default:
        throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
    }
    return UserIdentifier.builder().userType(userType).userId(userId).build();
  }

  // 포트원 결제 내역과 우리 시스템의 결제 요청 정보를 비교하여 검증하는 메소드
  private Integer verifyPaymentDetails(PaymentVerifyRequest request,
      Map<String, Object> portOnePayment) {
    String status = (String) portOnePayment.get("status");
    Integer paidAmount = (Integer) portOnePayment.get("amount");
    String merchantUid = (String) portOnePayment.get("merchant_uid");

    if (!"paid".equalsIgnoreCase(status)) {
      throw new CustomException(CustomErrorCode.PAYMENT_NOT_PAID);
    }
    if (!paidAmount.equals(request.getAmount())) {
      throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }
    if (!merchantUid.equals(request.getMerchantUid())) {
      throw new CustomException(CustomErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
    }
    return paidAmount;
  }

  // 결제 유형에 따라 상세 정보를 저장하는 메소드
  private Object savePaymentInfoDetails(PaymentVerifyRequest request, Integer paidAmount,
      Payment payment, PaymentStatus paymentStatus) {
    Object savedPaymentInfo;

    switch (request.getTargetType()) {
      case RESERVATION:
        Reservation reservation = reservationRepository.findById(request.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        ReservationPaymentInfo reservationPaymentInfo = paymentMapper.toReservationPaymentInfo(request,
            reservation, paidAmount, paymentStatus);
        savedPaymentInfo = reservationPaymentInfoRepository.save(reservationPaymentInfo);
        break;
      case AD:
        Advertisement advertisement = advertisementRepository.findById(request.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.ADVERTISEMENT_NOT_FOUND));
        AdFeeSetting adFeeSetting = adFeeSettingRepository.findByAdPositionIdAndIsActiveTrue(
                advertisement.getAdPosition().getId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));
        AdPaymentInfo adPaymentInfo = paymentMapper.toAdPaymentInfo(advertisement, adFeeSetting,
            paidAmount, paymentStatus);
        savedPaymentInfo = adPaymentInfoRepository.save(adPaymentInfo);
        break;
      case EXPO:
        Expo expo = expoRepository.findById(request.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        ExpoFeeSetting expoFeeSetting = expoFeeSettingRepository.findByIsActiveTrue()
            .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));
        ExpoPaymentInfo expoPaymentInfo = paymentMapper.toExpoPaymentInfo(expo, expoFeeSetting,
            paidAmount, paymentStatus);
        savedPaymentInfo = expoPaymentInfoRepository.save(expoPaymentInfo);
        break;
      default:
        throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
    }
    return savedPaymentInfo;
  }

  // 포트원 API에서 결제 정보를 조회하는 메소드
  private Map<String, Object> getPaymentInfo(String impUid, String accessToken) {
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