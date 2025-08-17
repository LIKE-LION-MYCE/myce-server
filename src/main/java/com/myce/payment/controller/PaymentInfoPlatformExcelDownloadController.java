package com.myce.payment.controller;

import com.myce.payment.service.PaymentInfoPlatformExcelDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/platform/payment-info/excel-download")
@RequiredArgsConstructor
public class PaymentInfoPlatformExcelDownloadController {
    private final PaymentInfoPlatformExcelDownloadService service;
    private final String EXCEL_FILE_NAME = "payment_info_xlsx";

    @GetMapping
    public void downloadExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate)
            throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + EXCEL_FILE_NAME + "\"");

        try {
            service.downloadExcel(response.getOutputStream(), type, keyword, startDate, endDate);
        } catch (IOException e) {
            log.error("Failed to write to OutputStream", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to generate and download file.");
        }
    }
}
