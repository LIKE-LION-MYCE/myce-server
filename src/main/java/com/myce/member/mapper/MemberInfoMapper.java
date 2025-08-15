package com.myce.member.mapper;

import com.myce.member.dto.MemberInfoResponse;
import com.myce.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberInfoMapper {
    
    public MemberInfoResponse toResponseDto(Member member) {
        return MemberInfoResponse.builder()
                .name(member.getName())
                .birth(member.getBirth())
                .loginId(member.getLoginId())
                .phone(member.getPhone())
                .email(member.getEmail())
                .gender(member.getGender())
                .gradeDescription(member.getMemberGrade().getDescription())
                .gradeImageUrl(member.getMemberGrade().getGradeImageUrl())
                .mileage(member.getMileage())
                .build();
    }
}