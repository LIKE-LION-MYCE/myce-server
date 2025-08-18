package com.myce.expo.service.mapper;

import com.myce.expo.dto.ExpoAdminInfoResponse;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.Expo;
import com.myce.member.entity.Member;

import java.util.List;

public class PlatformExpoMapper {
    public static ExpoAdminInfoResponse getExpoAdminInfoResponse(Expo expo, List<AdminCode> subAdmins) {
        Member member = expo.getMember();

        List<String> subAdminCode = subAdmins.stream().map((AdminCode::getCode)).toList();


        return ExpoAdminInfoResponse.builder()
                .superAdminNickname(member.getName())
                .superAdminEmail(member.getEmail())
                .superAdminUsername(member.getLoginId())
                .subAdmins(subAdminCode)
                .build();
    }
}
