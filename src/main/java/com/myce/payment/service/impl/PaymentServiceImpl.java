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
import com.myce.member.entity.Guest;
import com.myce.member.entity.Member;
import com.myce.member.repository.GuestRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.payment.config.PortOneConfig;
import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
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
import java.time.temporal.ChronoUnit;
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
  private final MemberRepository memberRepository;
  private final GuestRepository guestRepository;
  private final ReservationRepository reservationRepository;
  private final AdPaymentInfoRepository adPaymentInfoRepository;
  private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
  private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
  private final AdvertisementRepository advertisementRepository;
  private final ExpoRepository expoRepository;
  private final AdFeeSettingRepository adFeeSettingRepository;
  private final ExpoFeeSettingRepository expoFeeSettingRepository;


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

  // 결제 검증 및 저장
  @Override
  @Transactional
  public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
    // 1. 액세스 토큰 발급
    String accessToken = getAccessToken();

    // 2. 포트원 결제 내역 조회
    Map<String, Object> portOnePayment = getPaymentInfo(request.getImpUid(), accessToken);

    // 3. 결제 정보 검증
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

    // 4. 결제 주체 식별 (Member 또는 Guest) - 현재 Payment 엔티티에 직접 저장하지 않음
    Member member = null;
    Guest guest = null;

    switch (request.getTargetType()) {
      case RESERVATION:
        Reservation reservation = reservationRepository.findById(request.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        if (reservation.getUserType() == UserType.MEMBER) {
          member = memberRepository.findById(reservation.getUserId())
              .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        } else if (reservation.getUserType() == UserType.GUEST) {
          guest = guestRepository.findById(reservation.getUserId())
              .orElseThrow(() -> new CustomException(CustomErrorCode.GUEST_NOT_EXIST));
        }
        break;
      case AD:
      case EXPO:
        // AD와 EXPO는 회원만 결제 가능하므로, 현재 로그인된 회원 정보를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
          throw new CustomException(CustomErrorCode.REFRESH_TOKEN_NOT_EXIST);
        }
        // CustomUserDetails에서 memberId를 직접 가져옵니다.
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getMemberId(); // CustomUserDetails에 getMemberId()가 있다고 가정
        member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        break;
      default:
        throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
    }

    // 5. 검증 성공 시, Payment 엔티티 저장
    Payment payment = paymentMapper.toEntity(request, portOnePayment);
    paymentRepository.save(payment);

    // 6. target_type에 따라 해당 PaymentInfo 엔티티 저장
    Object paymentInfo = savePaymentInfoDetails(request, paidAmount, payment);

    // 7. PaymentVerifyResponse 생성 및 반환
    return paymentMapper.toPaymentVerifyResponse(payment, paymentInfo);
  }

  private Object savePaymentInfoDetails(PaymentVerifyRequest request, Integer paidAmount, Payment payment) {
    PaymentStatus paymentStatus = PaymentStatus.SUCCESS; // 결제 성공 상태
    Object savedPaymentInfo = null;

    switch (request.getTargetType()) {
      case RESERVATION:
        Reservation reservation = reservationRepository.findById(request.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        ReservationPaymentInfo reservationPaymentInfo = ReservationPaymentInfo.builder()
            .reservation(reservation)
            .totalAmount(paidAmount)
            .status(paymentStatus)
            .usedMileage(request.getUsedMileage()) // PaymentVerifyRequest에서 가져옴
            .savedMileage(request.getSavedMileage()) // PaymentVerifyRequest에서 가져옴
            .build();
        savedPaymentInfo = reservationPaymentInfoRepository.save(reservationPaymentInfo);
        break;
      case AD:
        Advertisement advertisement = advertisementRepository.findById(request.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.ADVERTISEMENT_NOT_FOUND));
        AdFeeSetting adFeeSetting = adFeeSettingRepository.findByAdPositionIdAndIsActiveTrue(advertisement.getAdPosition().getId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));
        AdPaymentInfo adPaymentInfo = AdPaymentInfo.builder()
            .advertisement(advertisement)
            .totalAmount(paidAmount)
            .status(paymentStatus)
            .totalDay(advertisement.getTotalDays())
            .feePerDay(adFeeSetting.getFeePerDay())
            .build();
        savedPaymentInfo = adPaymentInfoRepository.save(adPaymentInfo);
        break;
      case EXPO:
        Expo expo = expoRepository.findById(request.getTargetId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        ExpoFeeSetting expoFeeSetting = expoFeeSettingRepository.findByIsActiveTrue()
            .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));
        long totalDays = ChronoUnit.DAYS.between(expo.getDisplayStartDate(), expo.getDisplayEndDate()) + 1;
        ExpoPaymentInfo expoPaymentInfo = ExpoPaymentInfo.builder()
            .expo(expo)
            .totalAmount(paidAmount)
            .status(paymentStatus)
            .deposit(expoFeeSetting.getDeposit()) // ExpoFeeSetting에서 가져옴
            .premiumDeposit(expoFeeSetting.getPremiumDeposit()) // ExpoFeeSetting에서 가져옴
            .totalDay((int) totalDays) // 계산된 값
            .dailyUsageFee(expoFeeSetting.getDailyUsageFee()) // ExpoFeeSetting에서 가져옴
            .build();
        savedPaymentInfo = expoPaymentInfoRepository.save(expoPaymentInfo);
        break;
      default:
        throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
    }
    return savedPaymentInfo;
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
