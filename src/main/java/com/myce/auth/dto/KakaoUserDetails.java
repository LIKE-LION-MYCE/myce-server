package com.myce.auth.dto;

import com.myce.member.entity.type.Gender;
import com.myce.member.entity.type.ProviderType;
import java.time.LocalDate;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class KakaoUserDetails implements OAuth2UserInfo {

    private Map<String, Object> attributes;

    public KakaoUserDetails(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.KAKAO;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return String.valueOf(kakaoAccount.get("email"));
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return String.valueOf(properties.get("nickname"));
    }
}