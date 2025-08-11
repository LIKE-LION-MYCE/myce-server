package com.myce.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {
    
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}