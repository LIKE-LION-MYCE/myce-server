package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.*;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.advertisement.repository.AdvertisementRepository;
import com.myce.advertisement.service.PlatformAdminAdvertisementService;
import com.myce.advertisement.service.mapper.AdvertisementMapper;
import com.myce.common.dto.PageResponse;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.RejectInfo;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.common.repository.RejectInfoRepository;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.system.entity.AdFeeSetting;
import com.myce.system.repository.AdFeeSettingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformAdminAdvertisementServiceImpl implements PlatformAdminAdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final BusinessProfileRepository businessProfileRepository;
    // PlatformAdminAdvertisementDetailService로 분리 예정
    private final RejectInfoRepository rejectInfoRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final AdFeeSettingRepository adFeeSettingRepository;

    public PageResponse<SimpleApplyAdvertisement> getAllAdList(
            int page, int pageSize,
            boolean latestFirst, boolean isApply) {
        Sort sort = latestFirst ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        List<AdvertisementStatus> applyStatusList = getApplyStatusList(isApply);

        Page<Advertisement> bannerEntityPage = advertisementRepository
                .findByStatusIn(applyStatusList, pageable);

        return PageResponse.from(bannerEntityPage.map(this::getSimpleApplyAdvertisement));
    }

    public PageResponse<SimpleApplyAdvertisement> getFilteredAdListByKeyword(
            String keyword, String statusText,
            int page, int pageSize, boolean latestFirst, boolean isApply) {
        Sort sort = latestFirst ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<Advertisement> bannerEntityPage;

        List<AdvertisementStatus> applyStatusList = getApplyStatusList(isApply);
        AdvertisementStatus requestedStatus = AdvertisementStatus.fromString(statusText);

        if (requestedStatus != null && applyStatusList.contains(requestedStatus)) {
            bannerEntityPage = advertisementRepository
                    .findByTitleContainingAndStatus(keyword, requestedStatus, pageable);
        } else {
            bannerEntityPage = advertisementRepository
                    .findByTitleContainingAndStatusIn(keyword, applyStatusList, pageable);
        }

        return PageResponse.from(bannerEntityPage.map(this::getSimpleApplyAdvertisement));
    }

    public DetailApplyAdvertisement getDetail(Long bannerId) {
        Advertisement ad = advertisementRepository.findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BANNER_NOT_EXIST));

        return getDetailApplyAdvertisement(ad);
    }

    public AdPaymentInfoCheck generatePaymentCheck(Long bannerId) {
        Advertisement ad = advertisementRepository.findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BANNER_NOT_EXIST));
        AdFeeSetting feeSetting = adFeeSettingRepository
                .findByAdPositionId(ad.getAdPosition().getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));
        HashMap<String, Integer> priceMap = new HashMap<>();
        int totalPayment = 0;

        // todo: PG 수수료 고려 X
        int totalDayFee = feeSetting.getFeePerDay() * ad.getTotalDays();
        priceMap.put("총 이용료", totalDayFee);
        totalPayment += totalDayFee;

        return AdvertisementMapper.getAdPaymentForm(ad, priceMap, totalPayment);
    }

    @Transactional
    public void approveApply(Long bannerId, AdPaymentInfoRequest paymentInfoRequest) {
        Advertisement ad = advertisementRepository.findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BANNER_NOT_EXIST));
        AdFeeSetting feeSetting = adFeeSettingRepository
                .findByAdPositionId(ad.getAdPosition().getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));

        AdPaymentInfo paymentInfo = AdPaymentInfo.builder()
                .advertisement(ad)
                .status(PaymentStatus.PENDING)
                .totalAmount(feeSetting.getFeePerDay() * ad.getTotalDays())
                .totalDay(ad.getTotalDays())
                .feePerDay(feeSetting.getFeePerDay())
                .build();

        adPaymentInfoRepository.save(paymentInfo);
        ad.approve();
    }

    @Transactional
    public void rejectApply(Long bannerId, RejectAdRequest request) {
        Advertisement ad = advertisementRepository.findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ADVERTISEMENT_NOT_FOUND));
        RejectInfo rejectInfo = RejectInfo.builder()
                .targetType(TargetType.ADVERTISEMENT)
                .targetId(ad.getId())
                .description(request.getReason())
                .build();

        rejectInfoRepository.save(rejectInfo);
        ad.reject();
    }

    public AdCancelInfoCheck generateCancelCheck(Long bannerId) {
        Advertisement ad = advertisementRepository
                .findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ADVERTISEMENT_NOT_FOUND));
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(ad.getId(), PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        AdPaymentInfo adPayment = adPaymentInfoRepository
                .findByAdvertisementId(ad.getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        long remainDays = LocalDate.now().until(ad.getDisplayEndDate(), ChronoUnit.DAYS);
        Integer totalAmount = (int) remainDays * adPayment.getFeePerDay();
        return AdvertisementMapper.getAdCancelInfoCheck(payment, ad, totalAmount);
    }

    @Transactional
    public void cancelBanner(Long bannerId, AdCancelInfoRequest request) {
        Advertisement ad = advertisementRepository
                .findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ADVERTISEMENT_NOT_FOUND));
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(ad.getId(), PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        AdPaymentInfo adPayment = adPaymentInfoRepository
                .findByAdvertisementId(ad.getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        long remainDays = LocalDate.now().until(ad.getDisplayEndDate(), ChronoUnit.DAYS);
        Integer totalAmount = (int) remainDays * adPayment.getFeePerDay();

        Refund refund = Refund.builder()
                .isPartial(true)
                .payment(payment)
                .amount(totalAmount)
                .refundedAt(LocalDateTime.now())
                .status(RefundStatus.PENDING)
                .build();
        refundRepository.save(refund);

        ad.cancel();
    }

    public AdRejectInfoResponse getRejectInfo(Long bannerId) {
        RejectInfo rejectInfo = rejectInfoRepository
                .findByTargetIdAndTargetType(bannerId, TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REJECT_INFO_NOT_FOUND));

        return AdvertisementMapper.getAdRejectInfoResponse(rejectInfo);
    }

    public AdPaymentHistoryResponse getPaymentInfo(Long bannerId) {
        AdPaymentInfo paymentInfo = adPaymentInfoRepository
                .findByAdvertisementId(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        return AdvertisementMapper.getPaymentInfoRequest(paymentInfo);
    }

    public AdCancelHistoryResponse getCancelInfo(Long bannerId) {
        Advertisement advertisement = advertisementRepository
                .findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BANNER_NOT_EXIST));
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(bannerId, PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        Refund refund = refundRepository
                .findByPayment(payment)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REFUND_NOT_FOUND));

        return AdvertisementMapper.getAdCancelInfoResponse(advertisement, payment, refund);
    }

    private List<AdvertisementStatus> getApplyStatusList(boolean isApply) {
        if (isApply) {
            return List.of(AdvertisementStatus.PENDING_APPROVAL,
                    AdvertisementStatus.PENDING_PAYMENT,
                    AdvertisementStatus.PENDING_PUBLISH,
                    AdvertisementStatus.REJECTED,
                    AdvertisementStatus.CANCELLED,
                    AdvertisementStatus.COMPLETED);
        } else {
            return List.of(AdvertisementStatus.PUBLISHED,
                    AdvertisementStatus.PENDING_CANCEL);
        }
    }

    // DTO 변환
    private SimpleApplyAdvertisement getSimpleApplyAdvertisement(Advertisement advertisement) {
        BusinessProfile businessProfile = businessProfileRepository
                .findByTargetIdAndTargetType(advertisement.getId(), TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        return AdvertisementMapper.getSimpleAdvertisement(advertisement, businessProfile);
    }

    private DetailApplyAdvertisement getDetailApplyAdvertisement(Advertisement advertisement) {
        BusinessProfile businessProfile = businessProfileRepository
                .findByTargetIdAndTargetType(advertisement.getId(), TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        return AdvertisementMapper.getDetailAdvertisement(advertisement, businessProfile);
    }
}
