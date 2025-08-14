package com.myce.reservation.dto;

import com.myce.member.entity.type.Gender;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ExpoAdminPaymentDetailResponse {
    private String name;
    private String gender;
    private LocalDate birth;
    private String phone;
    private String email;
    private String ticketName;

    public ExpoAdminPaymentDetailResponse(
            String name,
            Gender gender,
            LocalDate birth,
            String phone,
            String email,
            String ticketName
    ){
        this.name = name;
        this.gender = gender.getLabel();
        this.birth = birth;
        this.email = email;
        this.phone = phone;
        this.ticketName = ticketName;
    }
}
