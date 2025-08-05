package com.myce.auth.dto;

import com.myce.member.entity.type.ProviderType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long memberId;
    private final String name;

    @Getter
    private final ProviderType providerType;

    private final String password;

    @Getter
    private final String role;

    @Builder
    public CustomUserDetails(Long memberId, String name, String password, ProviderType providerType, String role) {
        this.memberId = memberId;
        this.providerType = providerType;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.name;
    }
}
