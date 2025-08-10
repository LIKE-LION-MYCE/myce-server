package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ExpoAdminTicketResponseDto {
    private Long ticketId;
    private String name;
    private String type;
    private String description;
    private Integer price;
    private Integer totalQuantity;
    private LocalDate saleStartDate;
    private LocalDate saleEndDate;
    private LocalDate useStartDate;
    private LocalDate useEndDate;
}