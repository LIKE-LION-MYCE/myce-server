package com.myce.reservation.dto;

import com.myce.member.entity.type.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReserverBulkUpdateRequest {
    
    @NotEmpty(message = "예약자 정보는 비어있을 수 없습니다.")
    @Valid
    private List<ReserverInfo> reserverInfos;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReserverInfo {
        
        @NotNull(message = "예약자 ID는 필수입니다.")
        private Long reserverId;
        
        @NotBlank(message = "이름은 필수입니다.")
        private String name;
        
        private Gender gender;
        
        private String phone;
        
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;
    }
}