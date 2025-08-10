package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.*;
import com.myce.advertisement.dto.AdPaymentHistoryResponse;
import com.myce.advertisement.dto.AdRejectInfoResponse;
import com.myce.advertisement.dto.DetailApplyAdvertisement;
import com.myce.advertisement.dto.RejectAdRequest;
import com.myce.advertisement.dto.SimpleApplyAdvertisement;
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
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformAdminAdvertisementServiceImpl implements PlatformAdminAdvertisementService {

    @Autowired
    private AdvertisementRepository advertisementRepository;
    @Autowired
    private BusinessProfileRepository businessProfileRepository;
    // PlatformAdminAdvertisementDetailService로 분리 예정
    @Autowired
    private RejectInfoRepository rejectInfoRepository;
    @Autowired
    private AdPaymentInfoRepository adPaymentInfoRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private RefundRepository refundRepository;

    public PageResponse<SimpleApplyAdvertisement> getAllAdList(
            int page, int pageSize,
            boolean latestFirst, boolean isApply) {
        Sort sort = latestFirst ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        List<AdvertisementStatus> applyStatusList = getApplyStatusList(isApply);

        Page<Advertisement> bannerEntityPage = advertisementRepository.findByStatusIn(applyStatusList, pageable);

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

    public AdRejectInfoResponse getRejectInfo(Long bannerId) {
        RejectInfo rejectInfo = rejectInfoRepository
                .findByTargetIdAndTargetType(bannerId, TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REJECT_INFO_NOT_FOUND));

        return AdvertisementMapper.getAdRejectInfoResponse(rejectInfo);
    }

    public AdPaymentInfoResponse getPaymentInfo(Long bannerId) {
        AdPaymentInfo paymentInfo = adPaymentInfoRepository
                .findByAdvertisementId(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        return AdvertisementMapper.getPaymentInfoRequest(paymentInfo);
    }

    public AdCancelInfoResponse getCancelInfo(Long bannerId) {
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
