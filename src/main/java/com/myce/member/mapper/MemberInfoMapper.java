package com.myce.member.mapper;

import com.myce.member.dto.MemberInfoResponseDto;
import com.myce.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberInfoMapper {
    
    public MemberInfoResponseDto toResponseDto(Member member) {
        return MemberInfoResponseDto.builder()
                .name(member.getName())
                .birth(member.getBirth())
                .loginId(member.getLoginId())
                .phone(member.getPhone())
                .email(member.getEmail())
                .gender(member.getGender())
                .build();
    }
}