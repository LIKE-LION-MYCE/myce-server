package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DailyReservation {
    private LocalDate date;
    private String dayOfWeek;
    private Long reservationCount;
    
    @Builder
    public DailyReservation(LocalDate date, String dayOfWeek, Long reservationCount) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.reservationCount = reservationCount;
    }
}