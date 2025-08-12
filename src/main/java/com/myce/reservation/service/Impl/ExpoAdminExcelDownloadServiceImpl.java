package com.myce.reservation.service.Impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.member.entity.type.Gender;
import com.myce.reservation.dto.ReservationListInfoData;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.reservation.service.ExpoAdminExcelDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoAdminExcelDownloadServiceImpl implements ExpoAdminExcelDownloadService {

    private final ExpoRepository expoRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final ReserverRepository reserverRepository;

    private final String[] HEADERS = {"번호", "예약 코드", "이름", "성별", "생년월일", "전화번호", "이메일", "티켓 이름"};
    private final String SHEET_NAME = "예약자_명단";

    @Override
    @Transactional(readOnly = true)
    public void downloadMyReservationExcelFile(Long expoId, Long memberId, LoginType loginType, OutputStream outputStream) {

        validateMyAccess(expoId, memberId, loginType);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);

        try {
            // 1) 시트 생성
            SXSSFSheet sheet = workbook.createSheet(SHEET_NAME);
            sheet.trackAllColumnsForAutoSizing();

            // 2) 스타일 생성
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyle = createBodyCellStyle(workbook);
            CellStyle dateCellStyle = createDateCellStyle(workbook);

            // 3) 헤더 생성
            createHeaderRow(sheet, HEADERS, headerStyle);

            // 4) 헤더 고정 및 필터
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            // 5) DB 스트림으로 데이터 채우기
            try (Stream<ReservationListInfoData> data = reserverRepository.streamAllForExcel(expoId)) {
                AtomicInteger rowNum = new AtomicInteger(1);
                data.forEach(dto -> {
                    Row row = sheet.createRow(rowNum.get());
                    fillDataRow(dto, row, rowNum.getAndIncrement(), bodyStyle, dateCellStyle);
                });
            }

            // 6) 고정 컬럼 폭 적용
            applyFixedWidths(sheet);

            // 7) OutputStream에 쓰기
            workbook.write(outputStream);
            log.info("[ExcelDownload] Excel file written successfully for expoId={}", expoId);

        } catch (IOException e) {
            log.error("[ExcelDownload] Failed to write Excel file for expoId={}", expoId, e);
            throw new CustomException(CustomErrorCode.EXCEL_EXPORT_FAILED);
        } finally {
            workbook.dispose();
        }
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

    // 헤더 생성
    private void createHeaderRow(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }
    }
    
    // 행 생성
    private void fillDataRow(ReservationListInfoData dto,
                             Row row,
                             int rowNum,
                             CellStyle bodyStyle,
                             CellStyle dateCellStyle) {
        int index = 0;
        setCell(row, index++, rowNum, bodyStyle);
        setCell(row, index++, dto.getReservationCode(), bodyStyle);
        setCell(row, index++, dto.getName(), bodyStyle);
        setCell(row, index++, Gender.toLabel(dto.getGender()), bodyStyle);
        setCell(row, index++, dto.getBirthday(), bodyStyle, dateCellStyle);
        setCell(row, index++, dto.getPhone(), bodyStyle);
        setCell(row, index++, dto.getEmail(), bodyStyle);
        setCell(row, index, dto.getTicketName(), bodyStyle);
    }

    private void setCell(Row row, int index, String value, CellStyle bodyStyle) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(bodyStyle);
    }

    private void setCell(Row row, int index, int value, CellStyle bodyStyle) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(bodyStyle);
    }

    private void setCell(Row row, int index, LocalDate value, CellStyle bodyStyle, CellStyle dateCellStyle) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(bodyStyle);
        cell.setCellStyle(dateCellStyle);
    }

    // 고정 컬럼 폭 적용
    private void applyFixedWidths(Sheet sheet) {
        int[] widths = {5, 20, 12, 6, 12, 16, 28, 50};
        for (int i = 0; i < widths.length; i++) {
            int width = Math.min((widths[i] + 2) * 256, 255 * 256);
            sheet.setColumnWidth(i, width);
        }
    }
    
    //권한 설정
    private void validateMyAccess(Long expoId, Long memberId, LoginType loginType) {
        if (memberId == null || loginType == null) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }
        switch (loginType) {
            case MEMBER -> {
                if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            case ADMIN_CODE -> {
                if (!adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsReserverListViewTrue(memberId, expoId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }
}