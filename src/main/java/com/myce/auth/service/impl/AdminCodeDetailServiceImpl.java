package com.myce.auth.service.impl;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.auth.service.AdminCodeDetailService;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.member.entity.Member;
import com.myce.member.entity.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCodeDetailServiceImpl implements AdminCodeDetailService {

    private final AdminCodeRepository adminCodeRepository;

    @Override
    public UserDetails loadLoginIdAndCode(String loginId, String code) {
        AdminCode adminCode = adminCodeRepository.findByCode(code)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid code."));

        Member superMember = adminCode.getSuperMember();
        if (!superMember.getLoginId().equals(loginId)) {
            throw new UsernameNotFoundException("Invalid super admin login id.");
        }

        return CustomUserDetails.builder()
                .memberId(adminCode.getId())
                .loginId(code)
                .loginType(LoginType.ADMIN_CODE)
                .role(Role.EXPO_ADMIN.name())
                .build();
    }

    @Override
    public UserDetails loadCode(String code) {
        AdminCode adminCode = adminCodeRepository.findByCode(code)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid code."));

        return CustomUserDetails.builder()
                .memberId(adminCode.getId())
                .loginId(code)
                .loginType(LoginType.ADMIN_CODE)
                .role(Role.EXPO_ADMIN.name())
                .build();
    }
}
