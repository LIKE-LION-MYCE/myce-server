package com.myce.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ExpoAdminPaymentDetailResponse {
    private String name;
    private String gender;
    private LocalDate birth;
    private String phone;
    private String email;
}
