package com.myce.payment.service;


import java.io.OutputStream;
import java.time.LocalDate;

public interface PaymentInfoPlatformExcelDownloadService {
    void downloadExcel(OutputStream outputStream, String type, String keyword,
                       LocalDate startDate, LocalDate endDate);
}
