package com.myce.member.mapper;

import com.myce.member.dto.MemberInfoResponse;
import com.myce.member.dto.MemberInfoWithMileageResponse;
import com.myce.member.entity.Member;
import java.math.BigDecimal;
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
                .build();
    }

    public MemberInfoWithMileageResponse toResponseDtoWithMileage(Member member, BigDecimal mileageRate) {
        return MemberInfoWithMileageResponse.builder()
            .name(member.getName())
            .birth(member.getBirth())
            .loginId(member.getLoginId())
            .phone(member.getPhone())
            .email(member.getEmail())
            .gender(member.getGender())
            .mileageRate(mileageRate)
            .build();
    }
}