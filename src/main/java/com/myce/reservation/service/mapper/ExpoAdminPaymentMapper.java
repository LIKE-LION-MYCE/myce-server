package com.myce.reservation.service.mapper;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.entity.Guest;
import com.myce.member.entity.Member;
import com.myce.member.repository.GuestRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.reservation.dto.ExpoAdminPaymentBasicResponse;
import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import com.myce.reservation.entity.code.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpoAdminPaymentMapper {

    private final MemberRepository memberRepository;
    private final GuestRepository guestRepository;

    public ExpoAdminPaymentResponse toDto(ExpoAdminPaymentBasicResponse response) {

        String name = null;
        String loginId = null;
        String phone = null;
        String email = null;

        if (response.getUserType() == UserType.MEMBER) {
            Member member = memberRepository.findById(response.getUserId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

            name = member.getName();
            loginId = member.getLoginId();
            phone = member.getPhone();
            email = member.getEmail();

        } else if (response.getUserType() == UserType.GUEST) {
            Guest guest = guestRepository.findById(response.getUserId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.GUEST_NOT_EXIST));

            name = guest.getName();
            loginId = "-";
            phone = guest.getPhone();
            email = guest.getEmail();
        }

        return ExpoAdminPaymentResponse.builder()
                .reservationCode(response.getReservationCode())
                .name(name)
                .userType(response.getUserType().getLabel())
                .loginId(loginId)
                .phone(phone)
                .email(email)
                .quantity(response.getQuantity())
                .totalAmount(response.getTotalAmount())
                .reservationStatus(response.getReservationStatus().getLabel())
                .createdAt(response.getCreatedAt())
                .build();
    }
}