package com.myce.qrcode.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ExpoAdminQrReissueRequest {

    private boolean selectAllMatching = false;

    private List<Long> reserverIds;

    @AssertTrue(message = "재발급 대상자는 비어있을 수 없습니다.")
    public boolean validateReserverIds() {
        if (selectAllMatching) return true;
        return reserverIds != null && !reserverIds.isEmpty();
    }
}