package com.myce.member.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdvertisementRepository;
import com.myce.member.dto.*;
import com.myce.member.entity.Favorite;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberSetting;
import com.myce.member.mapper.*;
import com.myce.member.repository.FavoriteRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.member.repository.MemberSettingRepository;
import com.myce.member.service.MemberService;
import com.myce.payment.repository.PaymentRepository;
import com.myce.reservation.entity.Reservation;
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

    @Override
    public List<ReservedExpoResponse> getReservedExpos(Long memberId) {
        List<Reservation> reservations = reservationRepository.findReservationsByMemberIdWithExpoAndTicket(memberId);
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
        List<Object[]> paymentHistoryData = paymentRepository.findReservationPaymentHistoryByMemberId(memberId);
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
}