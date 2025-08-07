package com.myce.member.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.dto.MemberInfoResponseDto;
import com.myce.member.dto.ReservedExpoResponseDto;
import com.myce.member.entity.Member;
import com.myce.member.mapper.MemberInfoMapper;
import com.myce.member.mapper.ReservedExpoMapper;
import com.myce.member.repository.MemberRepository;
import com.myce.member.service.MemberService;
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
    
    @Override
    public List<ReservedExpoResponseDto> getReservedExpos(Long memberId) {
        List<Reservation> reservations = reservationRepository.findReservationsByMemberIdWithExpoAndTicket(memberId);
        return reservedExpoMapper.toResponseDtoList(reservations);
    }
    
    @Override
    public MemberInfoResponseDto getMemberInfo(Long memberId) {
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
}