package com.myce.auth.service.impl;

import com.myce.auth.dto.SignupRequest;
import com.myce.auth.security.TokenCookieProvider;
import com.myce.auth.security.util.JwtUtil;
import com.myce.auth.service.AuthService;
import com.myce.auth.service.mapper.AuthMapper;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberGrade;
import com.myce.member.entity.type.GradeCode;
import com.myce.member.repository.MemberGradeRepository;
import com.myce.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final TokenCookieProvider tokenCookieProvider;
    private final MemberGradeRepository memberGradeRepository;

    public void signup(SignupRequest signupRequest) {
        MemberGrade memberGrade = memberGradeRepository.findByGradeCode(GradeCode.BRONZE).orElseThrow();
        String password = passwordEncoder.encode(signupRequest.getPassword());
        Member member = authMapper.signupRequestToMember(signupRequest, memberGrade, password);

        memberRepository.save(member);
    }

    @Override
    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshToken(request.getCookies());
        if (jwtUtil.isExpired(refreshToken)) {
            throw new CustomException(CustomErrorCode.EXPIRED_TOKEN);
        }

        jwtUtil.validateToken(refreshToken);

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new CustomException(CustomErrorCode.INVALID_TOKEN);
        }

        Long memberId = jwtUtil.getMemberIdFromToken(refreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_TOKEN));

        String[] tokens = getTokens(member);
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, tokens[0]);
        ResponseCookie cookie = tokenCookieProvider.getCookie(JwtUtil.REFRESH_TOKEN, refreshToken);
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String getRefreshToken(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(JwtUtil.REFRESH_TOKEN)) {
                return cookie.getValue();
            }
        }

        throw new CustomException(CustomErrorCode.REFRESH_TOKEN_NOT_EXIST);
    }

    private String[] getTokens(Member member) {
        Long memberId = member.getId();
        String loginId = member.getLoginId();
        String name = member.getName();
        String role = member.getRole().name();

        String accessToken = jwtUtil.createToken(JwtUtil.ACCESS_TOKEN, memberId, loginId, name, role);
        String refreshToken = jwtUtil.createToken(JwtUtil.REFRESH_TOKEN, memberId, loginId, name, role);
        return new String[]{accessToken, refreshToken};
    }
}
