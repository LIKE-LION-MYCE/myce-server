package com.myce.member.service.impl;

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
import com.myce.member.dto.expo.ExpoAdminCodeResponse;
import com.myce.member.dto.expo.ExpoPaymentDetailResponse;
import com.myce.member.dto.expo.ExpoRefundReceiptResponse;
import com.myce.member.dto.expo.ExpoSettlementReceiptResponse;
import com.myce.member.dto.expo.MemberExpoDetailResponse;
import com.myce.member.dto.expo.MemberExpoResponse;
import com.myce.member.mapper.expo.ExpoAdminCodeMapper;
import com.myce.member.mapper.expo.ExpoPaymentDetailMapper;
import com.myce.member.mapper.expo.ExpoRefundReceiptMapper;
import com.myce.member.mapper.expo.ExpoSettlementReceiptMapper;
import com.myce.member.mapper.expo.MemberExpoDetailMapper;
import com.myce.member.mapper.expo.MemberExpoMapper;
import com.myce.member.service.MemberExpoService;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
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
public class MemberExpoServiceImpl implements MemberExpoService {

    private final MemberExpoMapper memberExpoMapper;
    private final MemberExpoDetailMapper memberExpoDetailMapper;
    private final ExpoPaymentDetailMapper expoPaymentDetailMapper;
    private final ExpoAdminCodeMapper expoAdminCodeMapper;
    private final ExpoSettlementReceiptMapper expoSettlementReceiptMapper;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final ExpoRepository expoRepository;
    private final TicketRepository ticketRepository;
    private final AdminCodeRepository adminCodeRepository;
    private final ExpoFeeSettingRepository expoFeeSettingRepository;
    private final ExpoRefundReceiptMapper expoRefundReceiptMapper;

    @Override
    public Page<MemberExpoResponse> getMemberExpos(Long memberId, Pageable pageable) {
        Page<Expo> expos = expoRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        return expos.map(memberExpoMapper::toMemberExpoResponse);
    }

    @Override
    public MemberExpoDetailResponse getMemberExpoDetail(Long memberId, Long expoId) {
        // 박람회가 해당 회원의 것인지 확인
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        // 결제 정보 조회
        ExpoPaymentInfo paymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElse(null);

        // 티켓 목록 조회
        List<Ticket> tickets = ticketRepository.findByExpoIdOrderByCreatedAtAsc(expoId);

        // 사업자 정보 조회
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(expoId, TargetType.EXPO)
                .orElse(null);

        return memberExpoDetailMapper.toMemberExpoDetailResponse(expo, paymentInfo, tickets, businessProfile);
    }

    @Override
    @Transactional
    public void cancelExpo(Long memberId, Long expoId) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        expo.cancel();
    }

    @Override
    public ExpoPaymentDetailResponse getExpoPaymentDetail(Long memberId, Long expoId) {
        // 박람회 정보 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        // 사업자 정보 조회
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(expoId, TargetType.EXPO)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        // 박람회 결제 정보 조회
        ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        return expoPaymentDetailMapper.toExpoPaymentDetailResponse(expo, businessProfile, expoPaymentInfo);
    }

    @Override
    public List<ExpoAdminCodeResponse> getExpoAdminCodes(Long memberId, Long expoId) {
        // 박람회가 해당 회원의 것인지 확인
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        // 해당 박람회의 관리자 코드 5개 조회
        List<AdminCode> adminCodes = adminCodeRepository.findByExpoId(expoId);

        return expoAdminCodeMapper.toExpoAdminCodeResponseList(adminCodes);
    }

    @Override
    public ExpoSettlementReceiptResponse getExpoSettlementReceipt(Long memberId, Long expoId) {
        // 박람회가 해당 회원의 것인지 확인
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        // 해당 박람회의 티켓 목록 조회
        List<Ticket> tickets = ticketRepository.findByExpoId(expoId);

        // 현재 활성화된 수수료 설정 조회
        ExpoFeeSetting feeSetting = expoFeeSettingRepository.findByIsActiveTrue()
                .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));

        return expoSettlementReceiptMapper.toSettlementReceiptResponse(expo, tickets, feeSetting);
    }

    @Override
    public ExpoRefundReceiptResponse getExpoRefundReceipt(Long memberId, Long expoId) {
        // 박람회가 해당 회원의 것인지 확인
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        // 게시 중인 박람회만 환불 가능
        if (expo.getStatus() != com.myce.expo.entity.type.ExpoStatus.PUBLISHED) {
            throw new CustomException(CustomErrorCode.INVALID_EXPO_STATUS);
        }

        // 사업자 정보 조회
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(expoId,
                        TargetType.EXPO)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        // 박람회 결제 정보 조회
        ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        return expoRefundReceiptMapper.toRefundReceiptDto(expo, businessProfile, expoPaymentInfo);
    }
}