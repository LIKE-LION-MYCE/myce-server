package com.myce.member.dto.expo;

import com.myce.reservation.entity.code.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservedExpoResponse {
    
    private Long expoId;
    private String title;
    private String thumbnailUrl;
    private Integer ticketPrice;
    private Integer ticketCount;
    private String ticketName;
    private Long reservationId;
    private String reservationCode;
    private LocalDateTime createdAt;
    private ReservationStatus reservationStatus;
}