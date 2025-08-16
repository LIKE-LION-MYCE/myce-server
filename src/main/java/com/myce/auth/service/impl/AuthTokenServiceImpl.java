package com.myce.auth.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.auth.repository.RefreshTokenRepository;
import com.myce.auth.security.provider.TokenCookieProvider;
import com.myce.auth.security.util.JwtUtil;
import com.myce.auth.service.AuthTokenService;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.member.entity.Member;
import com.myce.member.entity.type.Role;
import com.myce.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTokenServiceImpl implements AuthTokenService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final TokenCookieProvider tokenCookieProvider;
    private final AdminCodeRepository adminCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;

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
        Long memberId = jwtUtil.getMemberIdFromToken(refreshToken);

        if(!checkValidRefreshToken(refreshToken, loginType, memberId)) {
            throw new CustomException(CustomErrorCode.INVALID_TOKEN);
        }

        // 타입에 따른 토큰 발급
        String[] tokens;
        if (loginType.equals(LoginType.MEMBER.name())) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_TOKEN));
            tokens = getTokens(loginType, memberId, member.getLoginId(), member.getRole().name());
        } else if (loginType.equals(LoginType.ADMIN_CODE.name())) {
            AdminCode adminCode = adminCodeRepository.findById(memberId)
                    .orElseThrow(() -> new UsernameNotFoundException(CustomErrorCode.MEMBER_NOT_EXIST.getMessage()));
            tokens = getTokens(loginType, memberId, adminCode.getCode(), Role.EXPO_ADMIN.name());
        } else {
            throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }

        refreshTokenRepository.save(loginType, memberId, tokens[1], jwtUtil.getRefreshTokenTime());

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, tokens[0]);
        ResponseCookie cookie = tokenCookieProvider.getCookie(JwtUtil.REFRESH_TOKEN, tokens[1]);
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

    private String[] getTokens(String loginType, Long memberId, String loginId, String role) {
        String accessToken = jwtUtil.createToken(JwtUtil.ACCESS_TOKEN, loginType, memberId, loginId, role);
        String refreshToken = jwtUtil.createToken(JwtUtil.REFRESH_TOKEN, loginType, memberId, loginId, role);
        return new String[]{accessToken, refreshToken};
    }

    private boolean checkValidRefreshToken(String refreshToken, String loginType, Long memberId) {
        String originRefreshToken = refreshTokenRepository.findByLoginTypeAndMemberId(loginType, memberId);
        return originRefreshToken != null && originRefreshToken.equals(refreshToken);
    }

}
