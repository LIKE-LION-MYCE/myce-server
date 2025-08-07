package com.myce.member.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdvertisementRepository;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.member.dto.*;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import com.myce.member.entity.Favorite;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberSetting;
import com.myce.member.mapper.*;
import com.myce.member.repository.FavoriteRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.member.repository.MemberSettingRepository;
import com.myce.member.service.MemberService;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
    
    private final ReservationRepository reservationRepository;
    private final ReservedExpoMapper reservedExpoMapper;
    private final MemberRepository memberRepository;
    private final MemberInfoMapper memberInfoMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentHistoryMapper paymentHistoryMapper;
    private final MemberSettingRepository memberSettingRepository;
    private final MemberSettingMapper memberSettingMapper;
    private final FavoriteExpoMapper favoriteExpoMapper;
    private final FavoriteRepository favoriteRepository;
    private final AdvertisementRepository advertisementRepository;
    private final MemberAdvertisementMapper memberAdvertisementMapper;
    private final AdvertisementDetailMapper advertisementDetailMapper;
    private final AdvertisementRefundReceiptMapper advertisementRefundReceiptMapper;
    private final BusinessProfileRepository businessProfileRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;

    @Override
    public List<ReservedExpoResponse> getReservedExpos(Long memberId) {
        List<Reservation> reservations = reservationRepository.findReservationsByUserTypeAndUserIdWithExpoAndTicket(
                UserType.MEMBER, memberId);
        return reservedExpoMapper.toResponseDtoList(reservations);
    }

    @Override
    public List<FavoriteExpoResponse> getFavoriteExpos(Long memberId) {
        List<Favorite> favorites = favoriteRepository.findByMemberId(memberId);
        return favoriteExpoMapper.toResponseDtoList(favorites);
    }
    
    @Override
    public MemberInfoResponse getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        return memberInfoMapper.toResponseDto(member);
    }
    
    @Override
    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        member.withdraw();
    }
    
    @Override
    public List<PaymentHistoryResponse> getPaymentHistory(Long memberId) {
        List<Object[]> paymentHistoryData = paymentRepository.findReservationPaymentHistoryByUserTypeAndUserId(
                UserType.MEMBER, memberId);
        return paymentHistoryMapper.toResponseDtoList(paymentHistoryData);
    }
    
    @Override
    public MemberSettingResponse getMemberSetting(Long memberId) {
        MemberSetting memberSetting = memberSettingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_SETTING_NOT_EXIST));
        return memberSettingMapper.toResponseDto(memberSetting);
    }
    
    @Override
    @Transactional
    public void updateMemberSetting(Long memberId, MemberSettingUpdateRequest request) {
        MemberSetting memberSetting = memberSettingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_SETTING_NOT_EXIST));
        
        memberSetting.updateSettings(
                request.getLanguage(),
                request.getFontSize(),
                request.getIsReceiveEmail(),
                request.getIsReceivePush()
        );
    }
    
    @Override
    public List<MemberAdvertisementResponse> getMemberAdvertisements(Long memberId) {
        List<Advertisement> advertisements = advertisementRepository.findByMemberIdWithAdPosition(memberId);
        return memberAdvertisementMapper.toResponseDtoList(advertisements);
    }
    
    @Override
    public AdvertisementDetailResponse getAdvertisementDetail(Long memberId, Long advertisementId) {

        Advertisement advertisement = advertisementRepository.findByIdAndMemberIdWithAdPosition(advertisementId, memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ADVERTISEMENT_NOT_FOUND));
        
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(advertisementId, TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));
        
        return advertisementDetailMapper.toResponseDto(advertisement, businessProfile);
    }
    
    @Override
    @Transactional
    public void cancelAdvertisement(Long memberId, Long advertisementId) {
        Advertisement advertisement = advertisementRepository.findByIdAndMemberIdWithAdPosition(advertisementId, memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ADVERTISEMENT_NOT_FOUND));
        
        advertisement.cancel();
    }
    
    @Override
    public AdvertisementPaymentDetailResponse getAdvertisementPaymentDetail(Long memberId, Long advertisementId) {
        // 광고 정보 조회
        Advertisement advertisement = advertisementRepository.findByIdAndMemberIdWithAdPosition(advertisementId, memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ADVERTISEMENT_NOT_FOUND));
        
        // 사업자 정보 조회
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(advertisementId, TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));
        
        // 광고 결제 정보 조회 (AdPaymentInfo 테이블에서)
        AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(advertisementId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        return AdvertisementPaymentDetailResponse.builder()
                .advertisementTitle(advertisement.getTitle())
                .applicantName(businessProfile.getCompanyName())
                .displayStartDate(advertisement.getDisplayStartDate())
                .displayEndDate(advertisement.getDisplayEndDate())
                .totalDays(adPaymentInfo.getTotalDay())
                .feePerDay(adPaymentInfo.getFeePerDay())
                .totalAmount(adPaymentInfo.getTotalAmount())
                .status(advertisement.getStatus())
                .build();
    }
    
    @Override
    public AdvertisementRefundReceiptResponse getAdvertisementRefundReceipt(Long memberId, Long advertisementId) {
        // 광고 정보 조회
        Advertisement advertisement = advertisementRepository.findByIdAndMemberIdWithAdPosition(advertisementId, memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ADVERTISEMENT_NOT_FOUND));
        
        // 게시 중인 광고만 환불 가능
        if (advertisement.getStatus() != com.myce.advertisement.entity.type.AdvertisementStatus.PUBLISHED) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        
        // 사업자 정보 조회
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(advertisementId, TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));
        
        // 광고 결제 정보 조회
        AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(advertisementId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        return advertisementRefundReceiptMapper.toRefundReceiptDto(advertisement, businessProfile, adPaymentInfo);
    }
}