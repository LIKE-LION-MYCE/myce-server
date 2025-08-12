package com.myce.reservation.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.reservation.service.ExpoAdminExcelDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/expos/{expoId}/reservations/excel-download")
@RequiredArgsConstructor
public class ExpoAdminExcelDownloadController {

    private final ExpoAdminExcelDownloadService service;
    private final String EXCEL_FILE_NAME = "예약자_명단_xlsx";

    @GetMapping
    public void downloadMyReservationExcelFile (
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            HttpServletResponse httpResponse) throws IOException {

        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();

        httpResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        httpResponse.setHeader("Content-Disposition", "attachment; filename=\"" + EXCEL_FILE_NAME + "\"");

        service.downloadMyReservationExcelFile(
                expoId,
                memberId,
                loginType,
                httpResponse.getOutputStream());
    }
}