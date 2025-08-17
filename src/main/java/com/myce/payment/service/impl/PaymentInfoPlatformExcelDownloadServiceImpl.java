package com.myce.payment.service.impl;

import com.myce.payment.dto.PaymentInfoResponse;
import com.myce.payment.service.PaymentInfoPlatformExcelDownloadService;
import com.myce.payment.service.PaymentInfoPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentInfoPlatformExcelDownloadServiceImpl
        implements PaymentInfoPlatformExcelDownloadService {
    private final PaymentInfoPlatformService paymentInfoPlatformService;

    private final String[] HEADERS = {"번호", "제목", "종류", "이용 시작", "이용 종료",
            "신청 일자", "등록금", "티켓 수익", "총 수익", "상태"};
    private final String SHEET_NAME = "결제_정보";

    @Override
    public void downloadExcel(OutputStream outputStream, String type, String keyword, LocalDate startDate, LocalDate endDate) {

        Predicate<PaymentInfoResponse> filterPredicate = paymentInfo ->
                (keyword == null || paymentInfo.getTitle().contains(keyword)) &&
                        (type == null || paymentInfo.getType().equalsIgnoreCase(type)) &&
                        (startDate == null || !paymentInfo.getCreatedAt().toLocalDate().isBefore(startDate)) &&
                        (endDate == null || !paymentInfo.getCreatedAt().toLocalDate().isAfter(endDate));

        List<PaymentInfoResponse> allPayments = paymentInfoPlatformService
                .fetchAndSortAllPayments(true, filterPredicate);
        log.info("allPayments size: {}", allPayments.size());

        try (Workbook workbook = new SXSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);

            applyFixedWidths(sheet);

            // 스타일 생성
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyle = createBodyCellStyle(workbook);
            CellStyle dateStyle = createDateCellStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // 헤더 생성 및 스타일 적용
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 본문 데이터 생성 및 스타일 적용
            int rowNum = 1;
            for (PaymentInfoResponse payment : allPayments) {
                Row row = sheet.createRow(rowNum++);

                // 각 셀에 스타일 적용
                row.createCell(0).setCellValue(payment.getId() != null ? payment.getId() : 0);
                row.getCell(0).setCellStyle(bodyStyle);
                row.createCell(1).setCellValue(payment.getTitle() != null ? payment.getTitle() : "-");
                row.getCell(1).setCellStyle(bodyStyle);
                row.createCell(2).setCellValue(payment.getType() != null ? payment.getType() : "-");
                row.getCell(2).setCellStyle(bodyStyle);

                // 날짜 셀은 날짜 스타일 적용
                Cell serviceStartAtCell = row.createCell(3);
                serviceStartAtCell.setCellValue(payment.getServiceStartAt() != null ? payment.getServiceStartAt().toString() : "-");
                serviceStartAtCell.setCellStyle(dateStyle);

                Cell serviceEndAtCell = row.createCell(4);
                serviceEndAtCell.setCellValue(payment.getServiceEndAt() != null ? payment.getServiceEndAt().toString() : "-");
                serviceEndAtCell.setCellStyle(dateStyle);

                Cell createdAtCell = row.createCell(5);
                createdAtCell.setCellValue(payment.getCreatedAt() != null ? payment.getCreatedAt().toLocalDate().toString() : "-");
                createdAtCell.setCellStyle(dateStyle);

                row.createCell(6).setCellValue(payment.getDeposit() != null ? payment.getDeposit() : 0);
                row.getCell(6).setCellStyle(currencyStyle);
                row.createCell(7).setCellValue(payment.getTicketBenefit() != null ? payment.getTicketBenefit().doubleValue() : 0.0);
                row.getCell(7).setCellStyle(currencyStyle);
                row.createCell(8).setCellValue(payment.getTotalBenefit() != null ? payment.getTotalBenefit().doubleValue() : 0.0);
                row.getCell(8).setCellStyle(currencyStyle);

                row.createCell(9).setCellValue(payment.getStatus() != null ? payment.getStatus() : "-");
                row.getCell(9).setCellStyle(bodyStyle);
            }

            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            log.error("Failed to download excel file: ", e);
            throw new RuntimeException("Excel 다운로드 실패", e);
        }
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }

    // 헤더 스타일 생성
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    // 고정 컬럼 폭 적용
    private void applyFixedWidths(Sheet sheet) {
        int[] widths = {5, 50, 12, 12, 12, 16, 18, 18, 18, 10};
        for (int i = 0; i < widths.length; i++) {
            int width = Math.min((widths[i] + 2) * 256, 255 * 256);
            sheet.setColumnWidth(i, width);
        }
    }

    // 본문 스타일 생성
    private CellStyle createBodyCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }

    // 날짜 스타일 생성
    private CellStyle createDateCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }
}