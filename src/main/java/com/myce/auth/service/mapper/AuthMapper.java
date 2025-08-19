package com.myce.auth.service.mapper;

import com.myce.auth.dto.CheckDuplicateResponse;
import com.myce.auth.dto.FindLoginIdResponse;
import com.myce.auth.dto.SignupRequest;
import com.myce.common.util.DateUtil;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberGrade;
import com.myce.member.entity.type.Gender;
import com.myce.member.entity.type.Role;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public Member signupRequestToMember(SignupRequest signupRequest, MemberGrade memberGrade, String password) {
        return Member.builder()
                .name(signupRequest.getName())
                .loginId(signupRequest.getLoginId())
                .memberGrade(memberGrade)
                .password(password)
                .email(signupRequest.getEmail())
                .birth(DateUtil.toDate(signupRequest.getBirth()))
                .phone(signupRequest.getPhone())
                .role(Role.USER)
                .gender(Gender.fromString(signupRequest.getGender()))
                .build();
    }

    public FindLoginIdResponse getFindLoginIdResponse(String loginId) {
        return new FindLoginIdResponse(loginId);
    }

    public CheckDuplicateResponse getDuplicateResponse(boolean isDuplicate) {
        return new CheckDuplicateResponse(isDuplicate);
    }
}
