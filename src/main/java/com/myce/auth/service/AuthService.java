package com.myce.auth.service;

import com.myce.auth.dto.CheckDuplicateResponse;
import com.myce.auth.dto.FindLoginIdRequest;
import com.myce.auth.dto.FindLoginIdResponse;
import com.myce.auth.dto.SignupRequest;
import com.myce.auth.dto.TempPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    void signup(SignupRequest signupRequest);

    FindLoginIdResponse getLoginId(String name, String email);

    void sendTempPasswordMail(TempPasswordRequest request);

    CheckDuplicateResponse checkDuplication(String loginId);

}
