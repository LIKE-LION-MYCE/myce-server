package com.myce.auth.service;

import com.myce.auth.dto.SignupRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    void signup(SignupRequest signupRequest);

    void reissueToken(HttpServletRequest request, HttpServletResponse response);
}
