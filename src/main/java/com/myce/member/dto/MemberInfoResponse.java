package com.myce.member.dto;

import com.myce.member.entity.type.Gender;
import java.time.LocalDateTime;
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

    private Long id;
    private String name;
    private LocalDate birth;
    private String loginId;
    private String phone;
    private String email;
    private Gender gender;
    private LocalDateTime createdAt;
    private String role;
    private boolean isDelete;
    private String gradeDescription;
    private String gradeImageUrl;
    private Integer mileage;
}