package com.myce.auth.service;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
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
        Member member = memberRepository.findByProviderTypeAndLoginId(ProviderType.LOCAL, loginId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        return CustomUserDetails.builder()
                .memberId(member.getId())
                .name(member.getName())
                .password(member.getPassword())
                .providerType(member.getProviderType())
                .role(member.getRole().name())
                .build();
    }
}
