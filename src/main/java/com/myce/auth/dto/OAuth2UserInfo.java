package com.myce.auth.dto;

import com.myce.member.entity.type.ProviderType;

public interface OAuth2UserInfo {
    ProviderType getProviderType();
    String getProviderId();
    String getEmail();
    String getName();
}
