package com.myce.auth.service.impl;

import com.myce.auth.dto.FindLoginIdRequest;
import com.myce.auth.dto.FindLoginIdResponse;
import com.myce.auth.dto.SignupRequest;
import com.myce.auth.dto.type.LoginType;
import com.myce.auth.security.provider.TokenCookieProvider;
import com.myce.auth.security.util.JwtUtil;
import com.myce.auth.service.AuthService;
import com.myce.auth.service.mapper.AuthMapper;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberGrade;
import com.myce.member.entity.type.GradeCode;
import com.myce.member.entity.type.Role;
import com.myce.member.repository.MemberGradeRepository;
import com.myce.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final AdminCodeRepository adminCodeRepository;
    private final MemberGradeRepository memberGradeRepository;

    public void signup(SignupRequest signupRequest) {
        MemberGrade memberGrade = memberGradeRepository.findByGradeCode(GradeCode.BRONZE).orElseThrow();
        String password = passwordEncoder.encode(signupRequest.getPassword());
        Member member = authMapper.signupRequestToMember(signupRequest, memberGrade, password);

        memberRepository.save(member);
    }

    @Override
    public FindLoginIdResponse getLoginId(FindLoginIdRequest request) {
        Member member = memberRepository.findByNameAndEmail(request.getName(), request.getEmail())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        return authMapper.getFindLoginIdResponse(member.getLoginId());
    }

    @Override
    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshToken(request.getCookies());
        if (jwtUtil.isExpired(refreshToken)) {
            throw new CustomException(CustomErrorCode.EXPIRED_TOKEN);
        }

        jwtUtil.validateToken(refreshToken);

        // 리프레쉬 토큰 여부 확인
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new CustomException(CustomErrorCode.INVALID_TOKEN);
        }

        String loginType = jwtUtil.getLoginTypeFromToken(refreshToken);
        Long id = jwtUtil.getMemberIdFromToken(refreshToken);

        // 타입에 따른 토큰 발급
        String[] tokens;
        if (loginType.equals(LoginType.MEMBER.name())) {
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_TOKEN));
            tokens = getTokens(loginType, id, member.getLoginId(), member.getRole().name());
        } else if (loginType.equals(LoginType.ADMIN_CODE.name())) {
            AdminCode adminCode = adminCodeRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid code."));
            tokens = getTokens(loginType, id, adminCode.getCode(), Role.EXPO_ADMIN.name());
        } else {
            throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }

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

    private String[] getTokens(String loginType, Long id, String loginId, String role) {
        String accessToken = jwtUtil.createToken(JwtUtil.ACCESS_TOKEN, loginType, id, loginId, role);
        String refreshToken = jwtUtil.createToken(JwtUtil.REFRESH_TOKEN, loginType, id, loginId, role);
        return new String[]{accessToken, refreshToken};
    }
}
