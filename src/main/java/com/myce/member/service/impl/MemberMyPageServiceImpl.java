package com.myce.member.service.impl;

import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdvertisementRepository;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.TicketRepository;
import com.myce.member.dto.*;
import com.myce.member.entity.Favorite;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberSetting;
import com.myce.member.mapper.*;
import com.myce.member.repository.FavoriteRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.member.repository.MemberSettingRepository;
import com.myce.member.service.MemberMyPageService;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.system.entity.ExpoFeeSetting;
import com.myce.system.repository.ExpoFeeSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberMyPageServiceImpl implements MemberMyPageService {

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
    private final AdvertisementPaymentDetailMapper advertisementPaymentDetailMapper;
    private final AdvertisementRefundReceiptMapper advertisementRefundReceiptMapper;
    private final MemberExpoMapper memberExpoMapper;
    private final MemberExpoDetailMapper memberExpoDetailMapper;
    private final ExpoPaymentDetailMapper expoPaymentDetailMapper;
    private final ExpoAdminCodeMapper expoAdminCodeMapper;
    private final ExpoSettlementReceiptMapper expoSettlementReceiptMapper;
    private final BusinessProfileRepository businessProfileRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final ExpoRepository expoRepository;
    private final TicketRepository ticketRepository;
    private final AdminCodeRepository adminCodeRepository;
    private final ExpoFeeSettingRepository expoFeeSettingRepository;
    private final ExpoRefundReceiptMapper expoRefundReceiptMapper;

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
    public void updateMemberInfo(Long memberId, MemberInfoUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        
        member.updateInfo(request.getPhone(), request.getEmail());
    }

    @Override
    public Page<PaymentHistoryResponse> getPaymentHistory(Long memberId, Pageable pageable) {
        Page<Object[]> paymentHistoryData = paymentRepository.findReservationPaymentHistoryByUserTypeAndUserId(
                UserType.MEMBER, memberId, pageable);
        return paymentHistoryData.map(paymentHistoryMapper::toResponseDto);
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

        memberSettingMapper.updateMemberSetting(memberSetting, request);
    }

}