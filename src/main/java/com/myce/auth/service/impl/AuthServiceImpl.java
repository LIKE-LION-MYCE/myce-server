package com.myce.auth.service.impl;

import com.myce.auth.dto.SignupRequest;
import com.myce.auth.service.AuthService;
import com.myce.auth.service.mapper.AuthMapper;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberGrade;
import com.myce.member.entity.type.GradeCode;
import com.myce.member.repository.MemberGradeRepository;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final MemberGradeRepository memberGradeRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequest signupRequest) {
        MemberGrade memberGrade = memberGradeRepository.findByGradeCode(GradeCode.BRONZE).orElseThrow();
        String password = passwordEncoder.encode(signupRequest.getPassword());
        Member member = authMapper.signupRequestToMember(signupRequest, memberGrade, password);

        memberRepository.save(member);
    }
}
