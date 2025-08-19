package com.myce.payment.dto;

import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.reservation.dto.ReserverBulkSaveRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReservationPaymentVerifyRequest {
    private String impUid;
    private String merchantUid;
    private Integer amount;
    private PaymentTargetType targetType;
    private Long targetId;
    private Integer usedMileage;
    private Integer savedMileage;
    
    // 박람회 결제 시 추가 정보
    private List<ReserverBulkSaveRequest.ReserverSaveInfo> reserverInfos;
    private Long ticketId;
    private Integer quantity;
    private String sessionId; // Redis 세션 ID 추가
}