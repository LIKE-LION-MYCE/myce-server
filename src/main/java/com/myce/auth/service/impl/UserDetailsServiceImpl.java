package com.myce.auth.service.impl;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.member.entity.Member;
import com.myce.member.entity.type.ProviderType;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자 토큰입니다."));

        return CustomUserDetails.builder()
                .loginType(LoginType.MEMBER)
                .memberId(member.getId())
                .name(member.getName())
                .loginId(loginId)
                .password(member.getPassword())
                .role(member.getRole().name())
                .build();
    }
}
