package com.myce.auth.service;

import com.myce.auth.dto.SignupRequest;

public interface AuthService {
    void signup(SignupRequest signupRequest);
}
