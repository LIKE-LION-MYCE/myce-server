package com.myce.member.dto;

import com.myce.member.entity.type.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberInfoResponse {
    
    private String name;
    private LocalDate birth;
    private String loginId;
    private String phone;
    private String email;
    private Gender gender;
}