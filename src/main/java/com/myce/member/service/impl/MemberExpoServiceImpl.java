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
import com.myce.member.repository.MemberRepository;
import com.myce.member.dto.expo.ExpoAdminCodeResponse;
import com.myce.member.dto.expo.ExpoPaymentDetailResponse;
import com.myce.member.dto.expo.ExpoRefundReceiptResponse;
import com.myce.member.dto.expo.ExpoSettlementReceiptResponse;
import com.myce.member.dto.expo.ExpoSettlementRequest;
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
import com.myce.settlement.service.SettlementExpoAdminService;
import com.myce.settlement.entity.Settlement;
import com.myce.settlement.repository.SettlementRepository;
import com.myce.expo.entity.type.ExpoStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
    private final ExpoRefundReceiptMapper expoRefundReceiptMapper;
    private final SettlementExpoAdminService settlementExpoAdminService;
    private final SettlementRepository settlementRepository;
    private final MemberRepository memberRepository;

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

        ExpoStatus currentStatus = expo.getStatus();
        
        // 상태별 취소 처리
        switch (currentStatus) {
            case PENDING_APPROVAL:
                // 승인대기 상태: status만 CANCELLED로 변경 (ExpoPaymentInfo 없음)
                expo.updateStatus(ExpoStatus.CANCELLED);
                log.info("박람회 승인대기 취소 - 상태만 변경: 박람회 ID {}", expoId);
                break;
                
            case PENDING_PAYMENT:
                // 결제대기 상태: status CANCELLED + ExpoPaymentInfo 삭제
                expo.updateStatus(ExpoStatus.CANCELLED);
                ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                        .orElse(null);
                if (expoPaymentInfo != null) {
                    expoPaymentInfoRepository.delete(expoPaymentInfo);
                    log.info("박람회 결제대기 취소 - ExpoPaymentInfo 삭제됨: 박람회 ID {}", expoId);
                }
                break;
                
            default:
                throw new CustomException(CustomErrorCode.INVALID_EXPO_STATUS);
        }
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

        // 박람회 결제 정보 조회 (결제 시점의 수수료율 사용)
        ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        // SETTLEMENT_REQUESTED 또는 COMPLETED 상태일 때 정산 정보 조회
        Settlement settlement = null;
        if (expo.getStatus() == ExpoStatus.SETTLEMENT_REQUESTED || expo.getStatus() == ExpoStatus.COMPLETED) {
            settlement = settlementRepository.findByExpoId(expoId).orElse(null);
        }
        
        // Mapper에서 모든 정보를 한번에 처리
        return expoSettlementReceiptMapper.toSettlementReceiptResponse(expo, tickets, expoPaymentInfo, settlement);
    }

    @Override
    public ExpoRefundReceiptResponse getExpoRefundReceipt(Long memberId, Long expoId) {
        // 박람회가 해당 회원의 것인지 확인
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        // 상태와 관계없이 환불 영수증 조회 가능 (UI에서 모든 영수증을 항상 표시하기 위해)
        // 실제 환불 신청은 별도 API에서 상태 검증

        // 사업자 정보 조회
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(expoId,
                        TargetType.EXPO)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        // 박람회 결제 정보 조회
        ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        return expoRefundReceiptMapper.toRefundReceiptDto(expo, businessProfile, expoPaymentInfo);
    }
    
    @Override
    @Transactional
    public void requestExpoSettlement(Long memberId, Long expoId, ExpoSettlementRequest request) {
        // 박람회가 해당 회원의 것인지 권한 확인
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        
        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        
        // Settlement 로직은 SettlementExpoAdminService로 완전 위임
        settlementExpoAdminService.requestSettlement(expoId, request);
        
        log.info("정산 신청 위임 완료 - 박람회 ID: {}, 회원 ID: {}", expoId, memberId);
    }
}