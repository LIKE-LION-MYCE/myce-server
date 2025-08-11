package com.myce.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ExpoAdminExcelDownloadResponse {
    private String reservationCode;
    private String name;
    private String gender;
    private LocalDate birthday;
    private String phone;
    private String email;
    private String ticketName;
}