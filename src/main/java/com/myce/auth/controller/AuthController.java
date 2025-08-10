package com.myce.auth.controller;

import com.myce.auth.dto.SignupRequest;
import com.myce.auth.dto.VerifyEmailCodeRequest;
import com.myce.auth.service.AuthService;
import com.myce.auth.dto.VerificationEmailRequest;
import com.myce.auth.service.AuthVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthVerificationService authVerificationService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest signupRequest) {
        authService.signup(signupRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        authService.reissueToken(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-verification/send")
    public ResponseEntity<Void> sendVerifyEmail(@RequestBody VerificationEmailRequest request) {
        authVerificationService.sendVerificationMail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-verification/verify")
    public ResponseEntity<Void> verifyVerificationCode(@RequestBody VerifyEmailCodeRequest request) {
        authVerificationService.verifyCode(request);
        return ResponseEntity.ok().build();
    }
}
