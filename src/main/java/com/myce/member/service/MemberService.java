package com.myce.member.service;

import com.myce.member.dto.MemberInfoResponseDto;
import com.myce.member.dto.ReservedExpoResponseDto;

import java.util.List;

public interface MemberService {
    
    List<ReservedExpoResponseDto> getReservedExpos(Long memberId);
    
    MemberInfoResponseDto getMemberInfo(Long memberId);
    
    void withdrawMember(Long memberId);
}