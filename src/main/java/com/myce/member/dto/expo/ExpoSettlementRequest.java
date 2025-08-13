package com.myce.member.dto.expo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExpoSettlementRequest {
    
    @NotBlank(message = "수취인명은 필수입니다")
    @Size(max = 100, message = "수취인명은 100자 이하여야 합니다")
    private String receiverName;
    
    @NotBlank(message = "은행명은 필수입니다") 
    @Size(max = 10, message = "은행명은 10자 이하여야 합니다")
    private String bankName;
    
    @NotBlank(message = "계좌번호는 필수입니다")
    @Size(max = 200, message = "계좌번호는 200자 이하여야 합니다") 
    private String bankAccount;
}