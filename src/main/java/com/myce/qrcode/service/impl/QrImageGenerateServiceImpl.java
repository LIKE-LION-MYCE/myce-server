package com.myce.qrcode.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.qrcode.service.QrImageGenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * ZXing 라이브러리를 사용한 QR 이미지 생성 서비스 구현체
 */
@Slf4j
@Service
public class QrImageGenerateServiceImpl implements QrImageGenerateService {

    private static final int QR_CODE_SIZE = 300;
    private static final String IMAGE_FORMAT = "PNG";

    @Override
    public byte[] generateQrImage(String token) {
        try {
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(token, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, IMAGE_FORMAT, out);

            return out.toByteArray();
        } catch (Exception e) {
            log.error("QR 이미지 생성 실패 - token: {}", token, e);
            throw new CustomException(CustomErrorCode.QR_GENERATION_FAILED);
        }
    }
}
