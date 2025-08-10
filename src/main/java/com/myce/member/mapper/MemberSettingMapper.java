package com.myce.member.mapper;

import com.myce.member.dto.MemberSettingResponse;
import com.myce.member.dto.MemberSettingUpdateRequest;
import com.myce.member.entity.MemberSetting;
import org.springframework.stereotype.Component;

@Component
public class MemberSettingMapper {
    
    public MemberSettingResponse toResponseDto(MemberSetting memberSetting) {
        return MemberSettingResponse.builder()
                .language(memberSetting.getLanguage())
                .fontSize(memberSetting.getFontSize())
                .isReceiveEmail(memberSetting.getIsReceiveEmail())
                .isReceivePush(memberSetting.getIsReceivePush())
                .build();
    }
    
    public void updateMemberSetting(MemberSetting memberSetting, MemberSettingUpdateRequest request) {
        memberSetting.updateSettings(
                request.getLanguage(),
                request.getFontSize()
        );
    }
}