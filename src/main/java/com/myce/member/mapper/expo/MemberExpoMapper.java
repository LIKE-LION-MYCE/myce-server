package com.myce.member.mapper.expo;

import com.myce.expo.entity.Expo;
import com.myce.member.dto.expo.MemberExpoResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberExpoMapper {
    
    public MemberExpoResponse toMemberExpoResponse(Expo expo) {
        return MemberExpoResponse.builder()
                .expoId(expo.getId())
                .title(expo.getTitle())
                .createdAt(expo.getCreatedAt())
                .displayStartDate(expo.getDisplayStartDate())
                .displayEndDate(expo.getDisplayEndDate())
                .location(expo.getLocation())
                .locationDetail(expo.getLocationDetail())
                .status(expo.getStatus())
                .isPremium(expo.getIsPremium())
                .build();
    }
    
    public List<MemberExpoResponse> toMemberExpoResponseList(List<Expo> expos) {
        return expos.stream()
                .map(this::toMemberExpoResponse)
                .toList();
    }
}