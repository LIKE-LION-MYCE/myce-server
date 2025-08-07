package com.myce.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteExpoResponse {

    private Long expoId;
    private String title;
    private String thumbnailUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String locationDetail;
}