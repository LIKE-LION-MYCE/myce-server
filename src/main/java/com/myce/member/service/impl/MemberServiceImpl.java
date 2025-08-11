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
import com.myce.member.dto.AdvertisementDetailResponse;
import com.myce.member.dto.AdvertisementPaymentDetailResponse;
import com.myce.member.dto.AdvertisementRefundReceiptResponse;
import com.myce.member.dto.ExpoAdminCodeResponse;
import com.myce.member.dto.ExpoPaymentDetailResponse;
import com.myce.member.mapper.ExpoRefundReceiptMapper;
import com.myce.member.dto.ExpoRefundReceiptResponse;
import com.myce.member.dto.ExpoSettlementReceiptResponse;
import com.myce.member.dto.FavoriteExpoResponse;
import com.myce.member.dto.MemberAdvertisementResponse;
import com.myce.member.dto.MemberExpoDetailResponse;
import com.myce.member.dto.MemberExpoResponse;
import com.myce.member.dto.MemberInfoResponse;
import com.myce.member.dto.MemberSettingResponse;
import com.myce.member.dto.MemberInfoUpdateRequest;
import com.myce.member.dto.MemberSettingUpdateRequest;
import com.myce.member.dto.PaymentHistoryResponse;
import com.myce.member.dto.ReservedExpoResponse;
import com.myce.member.entity.Favorite;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberSetting;
import com.myce.member.mapper.AdvertisementDetailMapper;
import com.myce.member.mapper.AdvertisementPaymentDetailMapper;
import com.myce.member.mapper.AdvertisementRefundReceiptMapper;
import com.myce.member.mapper.ExpoAdminCodeMapper;
import com.myce.member.mapper.ExpoPaymentDetailMapper;
import com.myce.member.mapper.ExpoSettlementReceiptMapper;
import com.myce.member.mapper.FavoriteExpoMapper;
import com.myce.member.mapper.MemberAdvertisementMapper;
import com.myce.member.mapper.MemberExpoDetailMapper;
import com.myce.member.mapper.MemberExpoMapper;
import com.myce.member.mapper.MemberInfoMapper;
import com.myce.member.mapper.MemberSettingMapper;
import com.myce.member.mapper.PaymentHistoryMapper;
import com.myce.member.mapper.ReservedExpoMapper;
import com.myce.member.repository.FavoriteRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.member.repository.MemberSettingRepository;
import com.myce.member.service.MemberService;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        member.withdraw();
    }
}