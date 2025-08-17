package com.myce.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.warn("CustomException 발생: [{}] {}", ex.getErrorCode().getErrorCode(), ex.getErrorCode().getMessage());
        CustomErrorCode errorCode = ex.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(errorCode.getErrorCode(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage;
        
        // FieldError 먼저 확인 (필드별 검증 오류)
        if (ex.getBindingResult().getFieldError() != null) {
            errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        // ObjectError 확인 (클래스 레벨 검증 오류)
        else if (ex.getBindingResult().getGlobalError() != null) {
            errorMessage = ex.getBindingResult().getGlobalError().getDefaultMessage();
        }
        // 기본 메시지
        else {
            errorMessage = "입력값이 올바르지 않습니다.";
        }
        
        ErrorResponse errorResponse = new ErrorResponse("VALID_ERROR", errorMessage);
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
