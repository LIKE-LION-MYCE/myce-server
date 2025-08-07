package com.myce.member.dto;

import com.myce.member.entity.type.FontSize;
import com.myce.member.entity.type.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSettingResponse {
    
    private Language language;
    private FontSize fontSize;
    private Boolean isReceiveEmail;
    private Boolean isReceivePush;
}