package com.myce.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthTokenService {

    void reissueToken(HttpServletRequest request, HttpServletResponse response);

}
