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

    private final String name;
    private final String email;
    private final Gender gender;
    private final LocalDate birth;
    private final String phone;
    private Map<String, Object> attributes;

    public KakaoUserDetails(Map<String, Object> attributes) {
        log.debug(String.valueOf(attributes.get("id")));

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.email = (String) kakaoAccount.get("email");
        this.name = String.valueOf(kakaoAccount.get("name"));
        String genderStr = String.valueOf(kakaoAccount.get("gender")).toUpperCase();
        this.gender = Gender.fromString(genderStr);
        int birthYear = Integer.parseInt((String) kakaoAccount.get("birthyear"));
        String birthDay = String.valueOf(kakaoAccount.get("birthday"));
        int birthMonth = Integer.parseInt(birthDay.substring(0, 2));
        int birthDayMonth = Integer.parseInt(birthDay.substring(2));
        this.birth = LocalDate.of(birthYear, birthMonth,  birthDayMonth);
        this.phone = formatPhoneNumber(String.valueOf(kakaoAccount.get("phone_number")));
        log.debug(String.valueOf(kakaoAccount.get("phone_number")));
        log.debug(phone);

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
        return this.email;
    }

    @Override
    public String getName() {
        return name;
    }

    private static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }

        return phoneNumber
                .replaceAll("\\+82\\s*", "0")  // +82 제거하고 0으로 시작
                .replaceAll("\\s+", "")        // 공백 제거
                .replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3"); // xxx-xxxx-xxxx 포맷
    }
}