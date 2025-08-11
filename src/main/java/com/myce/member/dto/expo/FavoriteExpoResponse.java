package com.myce.member.dto.expo;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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