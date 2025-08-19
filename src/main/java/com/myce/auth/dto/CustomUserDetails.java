package com.myce.auth.dto;

import com.myce.auth.dto.type.LoginType;
import com.myce.member.entity.type.ProviderType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final LoginType loginType;
    private final Long memberId;
    private final String name;
    private final String loginId;
    private final String password;
    private final String role;

    @Builder
    public CustomUserDetails(LoginType loginType, Long memberId, String name, String loginId,
            String password, ProviderType providerType, String role) {
        this.loginType = loginType;
        this.memberId = memberId;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    public Long getUserId(){
        return this.memberId;
    }
}
