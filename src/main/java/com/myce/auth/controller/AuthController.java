package com.myce.auth.controller;

import com.myce.auth.dto.CheckDuplicateResponse;
import com.myce.auth.dto.FindLoginIdResponse;
import com.myce.auth.dto.FindLoginIdRequest;
import com.myce.auth.dto.SignupRequest;
import com.myce.auth.dto.TempPasswordRequest;
import com.myce.auth.dto.VerifyEmailCodeRequest;
import com.myce.auth.service.AuthService;
import com.myce.auth.dto.VerificationEmailRequest;
import com.myce.auth.service.AuthTokenService;
import com.myce.auth.service.AuthVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthTokenService authTokenService;
    private final AuthVerificationService authVerificationService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest signupRequest) {
        authService.signup(signupRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/find-id")
    public ResponseEntity<FindLoginIdResponse> findLoginId(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "email") String email) {
        FindLoginIdResponse response = authService.getLoginId(name, email);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/password/temp")
    public ResponseEntity<Void> findLoginId(@RequestBody @Valid TempPasswordRequest request) {
        authService.sendTempPasswordMail(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<CheckDuplicateResponse> checkDuplicateLoginId(@RequestParam String loginId) {
        CheckDuplicateResponse response = authService.checkDuplication(loginId);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        authTokenService.reissueToken(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-verification/send")
    public ResponseEntity<Void> sendVerifyEmail(@RequestBody @Valid VerificationEmailRequest request) {
        authVerificationService.sendVerificationMail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-verification/verify")
    public ResponseEntity<Void> verifyVerificationCode(@RequestBody @Valid VerifyEmailCodeRequest request) {
        authVerificationService.verifyCode(request);
        return ResponseEntity.ok().build();
    }
}
