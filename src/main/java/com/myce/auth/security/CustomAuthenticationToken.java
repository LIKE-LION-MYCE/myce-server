package com.myce.auth.security;

import com.myce.auth.dto.type.LoginType;
import java.util.Collection;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

@Getter
public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final LoginType loginType;

    public CustomAuthenticationToken(Object principal, Object credentials, LoginType loginType) {
        super(principal, credentials);
        this.loginType = loginType;
    }

    public CustomAuthenticationToken(Object principal, Object credentials,
            Collection<? extends GrantedAuthority> authorities, LoginType loginType) {
        super(principal, credentials, authorities);
        this.loginType = loginType;
    }
}
