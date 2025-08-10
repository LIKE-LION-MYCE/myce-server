package com.myce.payment.dto;

import com.myce.reservation.entity.code.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentifier {
    private UserType userType;
    private Long userId;
}
