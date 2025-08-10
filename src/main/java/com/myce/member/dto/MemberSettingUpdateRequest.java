package com.myce.member.dto;

import com.myce.member.entity.type.FontSize;
import com.myce.member.entity.type.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberSettingUpdateRequest {
    
    private Language language;
    private FontSize fontSize;
}