package com.myce.member.dto.expo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String reservationCode;
}