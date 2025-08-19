package com.myce.auth.security.filter;

import com.myce.auth.dto.GoogleUserDetails;
import com.myce.auth.dto.KakaoUserDetails;
import com.myce.auth.dto.OAuth2UserInfo;
import com.myce.auth.dto.type.LoginType;
import com.myce.auth.repository.RefreshTokenRepository;
import com.myce.auth.security.provider.CheckProductProvider;
import com.myce.auth.security.provider.TokenCookieProvider;
import com.myce.auth.security.util.JwtUtil;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberGrade;
import com.myce.member.entity.SocialAccount;
import com.myce.member.entity.type.GradeCode;
import com.myce.member.entity.type.ProviderType;
import com.myce.member.entity.type.Role;
import com.myce.member.repository.MemberGradeRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.member.repository.SocialAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final TokenCookieProvider tokenCookieProvider;
    private final MemberRepository memberRepository;
    private final MemberGradeRepository memberGradeRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CheckProductProvider checkProductProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        String provider = oauth2Token.getAuthorizedClientRegistrationId();
        OAuth2User oauth2User = oauth2Token.getPrincipal();

        log.info("[OAuth2Login] OAuth2 로그인 성공 - Provider: {}, User: {}", provider, oauth2User.getName());
        log.debug("[OAuth2Login] OAuth2 사용자 정보: {}", oauth2User.getAttributes());

        String redirectUrl = checkProductProvider.getRedirectUrl();
        try {
            OAuth2UserInfo userInfo = getOAuth2UserInfo(provider, oauth2User);

            Member member = findOrCreateMember(userInfo);
            String loginType = LoginType.MEMBER.name();
            Long memberId = member.getId();
            String loginId = member.getLoginId();
            String role = member.getRole().name();

            String accessToken = jwtUtil.createToken(JwtUtil.ACCESS_TOKEN, loginType, memberId, loginId, role);
            String refreshToken = jwtUtil.createToken(JwtUtil.REFRESH_TOKEN, loginType, memberId, loginId, role);

            refreshTokenRepository.save(loginType, memberId, refreshToken, jwtUtil.getRefreshTokenTime());

            ResponseCookie refreshCookie = tokenCookieProvider.getCookie(JwtUtil.REFRESH_TOKEN, refreshToken);
            response.addHeader("Set-Cookie", refreshCookie.toString());

            String redirectUrlForSuccess = redirectUrl + "/oauth/success?provider=%s&token=%s";
            redirectUrlForSuccess = String.format(redirectUrlForSuccess, provider, accessToken);
            
            log.info("[OAuth2Login] Successfully login. loginId: {}, redirecting to: {}", 
                member.getLoginId(), redirectUrlForSuccess);
            response.sendRedirect(redirectUrlForSuccess);
        } catch (Exception e) {
            log.error("[OAuth2Login] OAuth2 로그인 처리 중 오류 발생", e);
            String redirectUrlForFail = redirectUrl + "/oauth/failure?error=" + e.getMessage();
            response.sendRedirect(redirectUrlForFail);
        }
    }


    private OAuth2UserInfo getOAuth2UserInfo(String provider, OAuth2User oauth2User) {
        return switch (provider.toLowerCase()) {
            case "google" -> new GoogleUserDetails(oauth2User.getAttributes());
            case "kakao" -> new KakaoUserDetails(oauth2User.getAttributes());
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth2 Provider: " + provider);
        };
    }


    private Member findOrCreateMember(OAuth2UserInfo userInfo) {
        // 1. 소셜 계정으로 기존 연동 찾기
        Optional<SocialAccount> socialAccount = socialAccountRepository
            .findByProviderTypeAndProviderId(userInfo.getProviderType(), userInfo.getProviderId());
        
        if (socialAccount.isPresent()) {
            // 기존 소셜 계정으로 로그인
            Member member = socialAccount.get().getMember();
            log.info("[OAuth2Login] 기존 소셜 계정 로그인: {}, Provider: {}", userInfo.getEmail(), userInfo.getProviderType());
            log.info("[OAuth2Login] Check member exist: {}", member.getLoginId());
            return member;
        }
        
        // 2. 같은 이메일의 기존 회원 찾기
        Optional<Member> memberOptional = memberRepository.findByEmail(userInfo.getEmail());
        
        if (memberOptional.isPresent()) {
            // 기존 회원에 소셜 계정 연동
            Member existingMember = memberOptional.get();
            createSocialAccount(existingMember, userInfo);
            log.info("[OAuth2Login] 기존 회원에 소셜 계정 연동: {}, Provider: {}",
                userInfo.getEmail(), userInfo.getProviderType());
            return existingMember;
        }
        
        // 3. 신규 회원 + 소셜 계정 생성
        Member newMember = createNewMember(userInfo);
        createSocialAccount(newMember, userInfo);
        return newMember;
    }


    private Member createNewMember(OAuth2UserInfo userInfo) {
        String loginId = userInfo.getProviderType().name().toLowerCase() + "_" + userInfo.getProviderId();
        
        MemberGrade defaultGrade = memberGradeRepository.findByGradeCode(GradeCode.BRONZE)
                .orElseThrow(() -> new RuntimeException("기본 회원 등급을 찾을 수 없습니다."));

        Member newMember = Member.builder()
                    .memberGrade(defaultGrade)
                    .loginId(loginId)
                    .name(userInfo.getName())
                    .email(userInfo.getEmail())
                    .role(Role.USER)
                    .build();

        log.info("[OAuth2Login] 새 소셜 로그인 회원 생성: {}, Provider: {}",
                userInfo.getEmail(), userInfo.getProviderType());
        return memberRepository.save(newMember);
    }


    private SocialAccount createSocialAccount(Member member, OAuth2UserInfo userInfo) {
        SocialAccount socialAccount = SocialAccount.builder()
                .member(member)
                .providerType(userInfo.getProviderType())
                .providerId(userInfo.getProviderId())
                .providerEmail(userInfo.getEmail())
                .providerName(userInfo.getName())
                .build();

        log.info("[OAuth2Login] 소셜 계정 생성: 회원ID={}, Provider={}, Email={}",
            member.getId(), userInfo.getProviderType(), userInfo.getEmail());
        
        return socialAccountRepository.save(socialAccount);
    }
}