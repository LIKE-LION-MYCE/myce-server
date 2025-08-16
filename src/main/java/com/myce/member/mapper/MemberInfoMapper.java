package com.myce.member.mapper;

import com.myce.member.dto.MemberInfoListResponse;
import com.myce.member.dto.MemberInfoResponse;
import com.myce.member.dto.MemberInfoWithMileageResponse;
import com.myce.member.entity.Member;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class MemberInfoMapper {
    
    public MemberInfoResponse toResponseDto(Member member) {
        return MemberInfoResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .birth(member.getBirth())
                .loginId(member.getLoginId())
                .phone(member.getPhone())
                .email(member.getEmail())
                .gender(member.getGender())
                .createdAt(member.getCreatedAt())
                .gradeDescription(member.getMemberGrade().getDescription())
                .gradeImageUrl(member.getMemberGrade().getGradeImageUrl())
                .mileage(member.getMileage())
                .build();
    }

    public MemberInfoListResponse toListResponseDto(Page<Member> members) {
        int currentPage = members.getNumber() + 1;
        int totalPages = members.getTotalPages();
        MemberInfoListResponse memberInfoListResponse = new MemberInfoListResponse(currentPage, totalPages);
        members.forEach((member) -> {
            MemberInfoResponse response = toResponseDto(member);
            memberInfoListResponse.addMemberInfo(response);
        });
        return memberInfoListResponse;
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
            .gradeDescription(member.getMemberGrade().getDescription())
            .gradeImageUrl(member.getMemberGrade().getGradeImageUrl())
            .mileage(member.getMileage())
            .build();
    }
}