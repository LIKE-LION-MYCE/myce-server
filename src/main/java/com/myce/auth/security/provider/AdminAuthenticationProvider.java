package com.myce.auth.security.provider;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.auth.security.CustomAuthenticationToken;
import com.myce.auth.service.AdminCodeDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthenticationProvider implements AuthenticationProvider {

    private final AdminCodeDetailService adminCodeDetailService;

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CustomAuthenticationToken token = (CustomAuthenticationToken) authentication;
        if (token.getLoginType() != LoginType.ADMIN_CODE) {
            return null;
        }

        String superMemberLoginId = token.getName();
        String code = token.getCredentials().toString();
        log.info("Receive admin code login., superLoginId={}, code={}", superMemberLoginId, code);

        CustomUserDetails userDetails = (CustomUserDetails)
                adminCodeDetailService.loadLoginIdAndCode(superMemberLoginId, code);
        log.debug("Receive admin code login.");
        return new CustomAuthenticationToken(userDetails, null,
                userDetails.getAuthorities(), LoginType.ADMIN_CODE);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
