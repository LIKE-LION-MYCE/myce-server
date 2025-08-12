package com.myce.reservation.dto;

import com.myce.member.entity.type.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ExcelReservationInfoData {
    private String reservationCode;
    private String name;
    private Gender gender;
    private LocalDate birthday;
    private String phone;
    private String email;
    private String ticketName;
}