package com.myce.auth.security.filter;

import com.myce.auth.security.provider.CheckProductProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final CheckProductProvider checkProductProvider;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        
        log.error("OAuth2 로그인 실패: {}", exception.getMessage(), exception);
        log.error("Request URI: {}", request.getRequestURI());
        log.error("Request Parameters: {}", request.getParameterMap());
        
        String errorMessage = exception.getMessage();
        if (errorMessage == null) {
            errorMessage = "OAuth2 인증에 실패했습니다.";
        }

        String redirectUrlForFail = checkProductProvider.getRedirectUrl() + "/oauth/failure?error=" + errorMessage;
        response.sendRedirect(redirectUrlForFail);
    }
}